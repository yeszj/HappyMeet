package cn.yanhu.agora.bean;

import org.litepal.crud.LitePalSupport;

/**
 * @author: zhengjun
 * created: 2023/7/26
 * desc:
 */
public class AgoraSdkCacheInfo extends LitePalSupport {
    private String fileSize;
    private long id;

    private Integer version;

    public AgoraSdkCacheInfo(String fileSize, Integer version) {
        this.fileSize = fileSize;
        this.version = version;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
