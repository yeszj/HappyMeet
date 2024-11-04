package cn.yanhu.agora.bean;


import org.litepal.crud.LitePalSupport;



public class BeautyBean extends LitePalSupport {
    private String type;
    private long id;
    private String icon;
    private String checkIcon;
    private String name;
    private String key;
    private Integer value;
    private String filterName;


    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCheckIcon() {
        return checkIcon;
    }

    public void setCheckIcon(String checkIcon) {
        this.checkIcon = checkIcon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }


}
