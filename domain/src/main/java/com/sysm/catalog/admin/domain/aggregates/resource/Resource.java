package com.sysm.catalog.admin.domain.aggregates.resource;

import com.sysm.catalog.admin.domain.ValueObject;

import java.util.Objects;

public class Resource extends ValueObject {

    private final byte[] content;
    private final String checksum;
    private final String contentType;
    private final String name;

    private Resource( final byte[] content, final String checksum, final String contentType, final String name) {
        this.content = Objects.requireNonNull(content);
        this.checksum = Objects.requireNonNull(checksum);
        this.contentType = Objects.requireNonNull(contentType);
        this.name = Objects.requireNonNull(name);
    }

    public static Resource with(final byte[] content, final String checkSum, final String contentType, final String name) {
        return new Resource(content, checkSum, contentType, name);
    }

    public String checksum() {
        return checksum;
    }

    public byte[] content() {
        return content;
    }

    public String contentType() {
        return contentType;
    }

    public String name() {
        return name;
    }
}