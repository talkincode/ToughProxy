package org.toughproxy.form;

import org.toughproxy.entity.User;

public class UserQuery extends User {

    private String keyword;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
