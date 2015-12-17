package io.square1.richtextlib.v2.parser;

import android.text.TextUtils;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MarkupTag {


    public final String tag;
    public boolean closeOnEnd;
    public boolean duplicateOnStart;
    public final Attributes attributes;
    public final List<String> elementClasses;
    public boolean discardOnClosing;

    private MarkupTag mParent;
    private ArrayList<MarkupTag> mChildren;

    private TagHandler mTagHandler;

    public MarkupTag(String tag, Attributes attributes) {
        mChildren = new ArrayList<>();
        this.tag = tag;
        this.discardOnClosing = false;
        this.closeOnEnd = true;
        this.duplicateOnStart = true;
        this.attributes = new AttributesImpl(attributes);
        String attributeClass = attributes.getValue("class");
        this.elementClasses = parseClassAttribute(attributeClass);
    }

    public void setParent(MarkupTag parent){
        mParent = parent;
    }
    public void addChild(MarkupTag tag){
        tag.setParent(this);
        mChildren.add(tag);
    }

    public final String getElementClass(){
        return attributes.getValue("class");
    }

    public void setTagHandler(TagHandler handler) {
        mTagHandler = handler;
    }

    public TagHandler getTagHandler() {
        return mTagHandler;
    }

    @Override
    public String toString() {
        return "MarkupTag{" + this.tag + "}";
    }

    public static List<String> parseClassAttribute(String classAttribute) {
        if (TextUtils.isEmpty(classAttribute)) {
            return new ArrayList<>();
        }
        classAttribute = classAttribute.trim().replaceAll(" +", " ");
        return Arrays.asList(classAttribute.split(" "));
    }
}