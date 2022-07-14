package com.sbdev.letschat;

public class TypingModel {

    boolean typing;

    public TypingModel(boolean typing) {
        this.typing = typing;
    }

    public TypingModel()
    {

    }

    public boolean isTyping() {
        return typing;
    }

    public void setTyping(boolean typing) {
        this.typing = typing;
    }
}
