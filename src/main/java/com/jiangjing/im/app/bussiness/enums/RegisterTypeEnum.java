package com.jiangjing.im.app.bussiness.enums;

/**
 * @author Admin
 */

public enum RegisterTypeEnum {

    /**
     * 1 username；2 MOBILE。
     */
    USERNAME(1),

    MOBILE(2),

    GITEE(3),

    GITHUB(4);
    ;

    private int code;

    RegisterTypeEnum(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}
