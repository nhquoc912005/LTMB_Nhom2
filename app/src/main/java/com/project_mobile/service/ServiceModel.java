// Module dịch vụ/tài sản Android.
// File này là model UI cho một item danh mục dịch vụ hoặc tài sản.
// Dữ liệu chính gồm id, tên, giá, đơn vị và icon.
package com.project_mobile.service;

import com.project_mobile.network.ApiModels.CatalogItemDto;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * ServiceModel là dữ liệu đã chuẩn hóa để ServiceAdapter hiển thị.
 * fromDto() là điểm map CatalogItemDto từ API sang model UI.
 */
public class ServiceModel {
    private final String id;
    private final String name;
    private final double price;
    private final String unit;
    private final String icon;

    public ServiceModel(String id, String name, double price, String unit, String icon) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.unit = unit;
        this.icon = icon;
    }

    /** Map DTO backend sang ServiceModel với fallback khi thiếu field. */
    public static ServiceModel fromDto(CatalogItemDto dto) {
        return new ServiceModel(
                dto.id != null ? dto.id : "",
                dto.name != null ? dto.name : "",
                dto.price != null ? dto.price : 0,
                dto.unit,
                dto.icon);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getUnit() {
        return unit;
    }

    public String getIcon() {
        return icon;
    }

    /** Format giá theo locale Việt Nam và thêm hậu tố đơn vị nếu có. */
    public String getFormattedPrice() {
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        String suffix = unit == null || unit.trim().isEmpty() ? "" : "/" + unit.trim();
        return formatter.format(Math.round(price)) + "đ" + suffix;
    }
}
