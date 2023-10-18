package org.example.spec;

import org.example.enums.HttpMethod;

import java.util.Objects;

public class URLSpec {

    private String url;

    private HttpMethod method;

    public URLSpec(String url, HttpMethod method) {
        this.url = url;
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        URLSpec urlSpec = (URLSpec) o;
        return url.equals(urlSpec.url) && method == urlSpec.method;
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, method);
    }
}
