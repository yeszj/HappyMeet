package cn.yanhu.agora.bean;


/**
 * @author: zhengjun
 * created: 2023/7/26
 * desc:
 */
public class BeautyFileCacheInfo {
    private String fileMd5;
    private long id;

    private int version;

    public BeautyFileCacheInfo(String fileMd5, int version) {
        this.fileMd5 = fileMd5;
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
