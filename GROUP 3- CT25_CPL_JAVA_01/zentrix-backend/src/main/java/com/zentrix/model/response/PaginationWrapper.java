package com.zentrix.model.response;

import java.util.List;
import org.springframework.data.domain.Page;
import lombok.Getter;

@Getter
public class PaginationWrapper<T extends List<?>> {
    private final T data;
    private final int page;
    private final int size;
    private final int totalPages;
    private final int totalElements;

    public PaginationWrapper(T data, int page, int size, int totalPages, int totalElements) {
        this.data = data;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    private PaginationWrapper(Builder<T> builder) {
        this(builder.data, builder.page, builder.size, builder.totalPages, builder.totalElements);
    }

    public PaginationObject exportPaginationInfo() {
        return PaginationObject.builder()
                .page(page)
                .size(size)
                .totalPages(totalPages)
                .totalElements(totalElements)
                .build();
    }

    public static class Builder<T extends List<?>> {
        private T data;
        private int page;
        private int size;
        private int totalPages;
        private int totalElements;

        public Builder<T> setData(T data) {
            this.data = data;
            return this;
        }

        public Builder<T> setPage(int page) {
            this.page = page;
            return this;
        }

        public Builder<T> setSize(int size) {
            this.size = size;
            return this;
        }

        public Builder<T> setTotalPages(int totalPages) {
            this.totalPages = totalPages;
            return this;
        }

        public Builder<T> setTotalElements(int totalElements) {
            this.totalElements = totalElements;
            return this;
        }

        public Builder<T> setPaginationInfo(Page<?> page) {
            if (page == null) {
                throw new IllegalArgumentException("Page cannot be null");
            }
            this.page = page.getNumber();
            this.size = page.getSize();
            this.totalElements = (int) page.getTotalElements();
            this.totalPages = page.getTotalPages();
            return this;
        }

        public PaginationWrapper<T> build() {
            // Kiểm tra dữ liệu đầu vào
            if (data == null) {
                throw new IllegalArgumentException("Data cannot be null");
            }
            if (size <= 0) {
                throw new IllegalArgumentException("Size must be greater than 0");
            }
            if (page < 0) {
                throw new IllegalArgumentException("Page must be non-negative");
            }
            if (totalElements < 0) {
                throw new IllegalArgumentException("Total elements must be non-negative");
            }
            if (totalPages < 0) {
                throw new IllegalArgumentException("Total pages must be non-negative");
            }

            // Kiểm tra nhất quán giữa data.size() và size/totalElements
            int dataSize = data.size();
            if (dataSize > size) {
                throw new IllegalStateException("Data size (" + dataSize + ") exceeds page size (" + size + ")");
            }
            if (totalElements < dataSize) {
                throw new IllegalStateException(
                        "Total elements (" + totalElements + ") is less than data size (" + dataSize + ")");
            }
            if (totalPages != (int) Math.ceil((double) totalElements / size)) {
                throw new IllegalStateException("Total pages (" + totalPages + ") does not match calculated value: " +
                        (int) Math.ceil((double) totalElements / size));
            }

            return new PaginationWrapper<T>(this);
        }
    }
}