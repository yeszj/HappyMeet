package cn.yanhu.commonres.bean;

/**
 * @author: witness
 * created: 2022/8/4
 * desc:
 */
public interface GiftIdentify extends Comparable<GiftIdentify>{


    /**
     * 礼物累计数
     * @return
     */
    int getTheGiftCount();

    void setTheGiftCount(int count);

    /**
     * 礼物停留时间
     * @return
     */
    long getTheGiftStay();

    void setTheGiftStay(long stay);

    /**
     * 礼物最新一次刷新时间戳
     * @return
     */
    long getTheLatestRefreshTime();

    void setTheLatestRefreshTime(long time);

    /**
     * 礼物索引
     * @return
     */
    int getTheCurrentIndex();

    void setTheCurrentIndex(int index);
}
