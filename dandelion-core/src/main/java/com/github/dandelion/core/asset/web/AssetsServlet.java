/*
 * [The "BSD licence"]
 * Copyright (c) 2013 Dandelion
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of Dandelion nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.dandelion.core.asset.web;

import com.github.dandelion.core.asset.AssetType;
import com.github.dandelion.core.config.Configuration;
import org.slf4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.github.dandelion.core.asset.AssetsCache.cache;
import static com.github.dandelion.core.asset.AssetsCache.getCacheKey;
import static com.github.dandelion.core.utils.DandelionUtils.isDevModeEnabled;

public abstract class AssetsServlet extends HttpServlet {
    public static final String DANDELION_ASSETS = "dandelionAssets";
    public static final String DANDELION_ASSETS_URL = "/dandelion-assets/";
    public static final String DANDELION_ASSETS_URL_PATTERN = "/dandelion-assets/*";
    private static final String ASSETS_CACHE_CONTROL = "assets.cache.control";
    private String cacheControl;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        getLogger().debug("Dandelion Asset servlet captured GET request {}", request.getRequestURI());

        String resource = request.getParameter("r");
        String cacheKey = getCacheKey(request);

        if (isDevModeEnabled() && !cache.containsKey(cacheKey))
            throw new ServletException("The Dandelion assets should have been generated!");

        AssetType resourceType = AssetType.typeOfAsset(resource);
        if (resourceType == null) return;

        String fileContent = cache.get(cacheKey);
        response.setContentType(resourceType.getContentType());
        response.setHeader("Cache-Control", getCacheControl());
        response.getWriter().write(fileContent);
    }

    protected abstract Logger getLogger();

    public String getCacheControl() {
        if(cacheControl == null) {
            initializeCacheControl();
        }
        return cacheControl;
    }

    synchronized private void initializeCacheControl() {
        if(cacheControl != null) return;
        String _cacheControl = Configuration.getProperty(ASSETS_CACHE_CONTROL);
        if(isDevModeEnabled() || _cacheControl == null || _cacheControl.isEmpty()) {
            _cacheControl = "no-cache";
        }
        cacheControl = _cacheControl;
    }
}
