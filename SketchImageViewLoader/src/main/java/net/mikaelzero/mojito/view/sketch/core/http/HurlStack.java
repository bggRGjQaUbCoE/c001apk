/*
 * Copyright (C) 2019 Peng fei Pan <panpfpanpf@outlook.me>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.mikaelzero.mojito.view.sketch.core.http;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.github.pixee.security.HostValidator;
import io.github.pixee.security.Urls;

import net.mikaelzero.mojito.view.sketch.core.util.SketchUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HurlStack implements HttpStack {
    private static final String KEY = "HurlStack";

    private int readTimeout = DEFAULT_READ_TIMEOUT;
    private int maxRetryCount = DEFAULT_MAX_RETRY_COUNT;
    private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
    private String userAgent;
    private Map<String, String> setExtraHeaders;
    private Map<String, String> addExtraHeaders;

    @Override
    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    @NonNull
    @Override
    public HurlStack setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
        return this;
    }

    @Override
    public int getConnectTimeout() {
        return connectTimeout;
    }

    @NonNull
    @Override
    public HurlStack setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    @Override
    public int getReadTimeout() {
        return readTimeout;
    }

    @NonNull
    @Override
    public HurlStack setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    @Override
    public String getUserAgent() {
        return userAgent;
    }

    @NonNull
    @Override
    public HurlStack setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    @Override
    public Map<String, String> getExtraHeaders() {
        return setExtraHeaders;
    }

    @NonNull
    @Override
    public HurlStack setExtraHeaders(Map<String, String> extraHeaders) {
        this.setExtraHeaders = extraHeaders;
        return this;
    }

    @Override
    public Map<String, String> getAddExtraHeaders() {
        return addExtraHeaders;
    }

    @NonNull
    @Override
    public HurlStack addExtraHeaders(Map<String, String> extraHeaders) {
        this.addExtraHeaders = extraHeaders;
        return this;
    }

    @Override
    public boolean canRetry(@NonNull Throwable throwable) {
        return throwable instanceof SocketTimeoutException;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.US, "%s(maxRetryCount=%d,connectTimeout=%d,readTimeout=%d,userAgent=%s)",
                KEY, maxRetryCount, connectTimeout, readTimeout, userAgent);
    }

    @NonNull
    @Override
    public Response getResponse(String uri) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) Urls.create(uri, Urls.HTTP_PROTOCOLS, HostValidator.DENY_COMMON_INFRASTRUCTURE_TARGETS).openConnection();

        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(readTimeout);
        connection.setDoInput(true);

        if (userAgent != null) {
            connection.setRequestProperty("User-Agent", userAgent);
        }

        if (addExtraHeaders != null && addExtraHeaders.size() > 0) {
            for (Map.Entry<String, String> entry : addExtraHeaders.entrySet()) {
                connection.addRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        if (setExtraHeaders != null && setExtraHeaders.size() > 0) {
            for (Map.Entry<String, String> entry : setExtraHeaders.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }

        processRequest(uri, connection);

        connection.connect();

        return new HurlResponse(connection);
    }

    protected void processRequest(@SuppressWarnings("UnusedParameters") String uri,
                                  @SuppressWarnings("UnusedParameters") HttpURLConnection connection) {

    }

    private static class HurlResponse implements Response {
        private HttpURLConnection connection;

        HurlResponse(HttpURLConnection connection) {
            this.connection = connection;
        }

        @Override
        public int getCode() throws IOException {
            return connection.getResponseCode();
        }

        @Nullable
        @Override
        public String getMessage() throws IOException {
            return connection.getResponseMessage();
        }

        @Override
        public long getContentLength() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return connection.getContentLengthLong();
            } else {
                return getHeaderFieldLong("content-length", -1);
            }
        }

        @Nullable
        @Override
        public String getContentType() {
            return connection.getContentType();
        }

        @Override
        public String getHeadersString() {
            Map<String, List<String>> headers = connection.getHeaderFields();
            if (headers == null || headers.size() == 0) {
                return null;
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("[");
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                if (stringBuilder.length() != 1) {
                    stringBuilder.append(", ");
                }

                stringBuilder.append("{");

                stringBuilder.append(entry.getKey());

                stringBuilder.append(":");

                List<String> values = entry.getValue();
                if (values.size() == 0) {
                    stringBuilder.append("");
                } else if (values.size() == 1) {
                    stringBuilder.append(values.get(0));
                } else {
                    stringBuilder.append(values.toString());
                }

                stringBuilder.append("}");
            }
            stringBuilder.append("]");
            return stringBuilder.toString();
        }

        @NonNull
        @Override
        public InputStream getContent() throws IOException {
            return connection.getInputStream();
        }

        @Override
        public void releaseConnection() {
            try {
                SketchUtils.close(getContent());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean isContentChunked() {
            String transferEncodingValue = connection.getHeaderField("Transfer-Encoding");
            if (transferEncodingValue != null) {
                transferEncodingValue = transferEncodingValue.trim();
            }
            return "chunked".equalsIgnoreCase(transferEncodingValue);
        }

        @Nullable
        @Override
        public String getContentEncoding() {
            return connection.getContentEncoding();
        }

        @Nullable
        @Override
        public String getHeaderField(@NonNull String name) {
            return connection.getHeaderField(name);
        }

        @Override
        public int getHeaderFieldInt(@NonNull String name, int defaultValue) {
            return connection.getHeaderFieldInt(name, defaultValue);
        }

        @Override
        public long getHeaderFieldLong(@NonNull String name, long defaultValue) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return connection.getHeaderFieldLong(name, defaultValue);
            } else {
                String value = connection.getHeaderField(name);
                try {
                    return Long.parseLong(value);
                } catch (Exception e) {
                    return defaultValue;
                }
            }
        }
    }
}
