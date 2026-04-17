package com.backend.api.controller;

import com.backend.api.dto.CheckInDTO;
import com.backend.api.entity.Booking;
import com.backend.api.entity.StayRecord;
import com.backend.api.repository.BookingRepository;
import com.backend.api.repository.StayRecordRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/checkin")
public class CheckInController {

    private final BookingRepository bookingRepository;
    private final StayRecordRepository stayRecordRepository;

    public CheckInController(BookingRepository bookingRepository, StayRecordRepository stayRecordRepository) {
        this.bookingRepository = bookingRepository;
        this.stayRecordRepository = stayRecordRepository;
    }

    @GetMapping("/pending")
    public List<CheckInDTO> getPendingCheckIns() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return bookingRepository.findByStatus("Chờ check-in").stream().map(b -> {
            String period = b.getCheckInDate().format(formatter) + " - " + b.getCheckOutDate().format(formatter);
            return new CheckInDTO(b.getId(), b.getGuestName(), b.getRoomNumber(), b.getPhone(), b.getEmail(), period);
        }).collect(Collectors.toList());
    }

    @PostMapping("/confirm/{id}")
    public ResponseEntity<?> confirmCheckIn(@PathVariable Integer id) {
        return bookingRepository.findById(id).map(booking -> {
            // 1. Update status
            booking.setStatus("Đã check-in");
            bookingRepository.save(booking);

            // 2. Create stay record
            StayRecord stay = new StayRecord();
            stay.setBookingId(booking.getId());
            stay.setActualCheckIn(LocalDateTime.now());
            stayRecordRepository.save(stay);

            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
