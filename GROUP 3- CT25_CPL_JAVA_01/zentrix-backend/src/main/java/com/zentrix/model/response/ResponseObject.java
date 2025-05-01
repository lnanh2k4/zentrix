package com.zentrix.model.response;

import java.util.List;

import lombok.Getter;

@Getter
public class ResponseObject<T> {
    private final boolean success;
    private final int code;
    private final String message;
    private final PaginationObject pagination;
    private final T content;

    private ResponseObject(Builder<T> builder) {
        this.success = builder.success;
        this.code = builder.code;
        this.message = builder.message;
        this.pagination = builder.pagination;
        this.content = builder.content;
    }

    public static class Builder<T> {
        private boolean success;
        private int code;
        private String message;
        private PaginationObject pagination;
        private T content;

        public Builder<T> message(String message) {
            this.message = message;
            return this;
        }

        public Builder<T> code(int code) {
            this.code = code;
            return this;
        }

        public Builder<T> success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder<T> content(T content) {
            this.content = content;
            return this;
        }

        public Builder<T> unwrapPaginationWrapper(PaginationWrapper<? extends List<?>> wrapper) {
            if (wrapper != null && wrapper.getData() != null) {
                try {
                    @SuppressWarnings("unchecked")
                    T data = (T) wrapper.getData();
                    this.content = data;
                } catch (ClassCastException e) {
                    throw new IllegalArgumentException("Invalid type for content. Expected a type compatible with T.",
                            e);
                }
                this.pagination = wrapper.exportPaginationInfo();
            } else {
                throw new IllegalArgumentException("Invalid type: wrapper data is not a List.");
            }
            return this;
        }

        public Builder<T> pagination(PaginationObject pagination) {
            this.pagination = pagination;
            return this;
        }

        public ResponseObject<T> build() {
            return new ResponseObject<T>(this);
        }
    }
}
