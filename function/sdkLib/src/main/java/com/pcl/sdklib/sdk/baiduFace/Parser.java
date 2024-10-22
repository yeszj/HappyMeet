/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.pcl.sdklib.sdk.baiduFace;


import com.baidu.idl.face.api.exception.FaceException;

/**
 * JSON解析
 * @param <T>
 */
public interface Parser<T> {
    T parse(String json) throws FaceException;
}
