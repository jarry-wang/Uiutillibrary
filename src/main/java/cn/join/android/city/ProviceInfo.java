package cn.join.android.city;

import java.io.Serializable;

/**
 * Created by mlfdev1 on 2015/11/25.
 */
public class ProviceInfo implements Serializable {
    private String name;
    private String typeName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String toString() {
        return "ProviceInfo [name=" + name + ", typeName=" + typeName + "]";
    }
}
