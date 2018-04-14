package org.rambo.nfis.pojo;

import java.util.ArrayList;

/**
 * @author Rambo Yang
 */
public class Res {

    private String id = "";
    private String type = "unknown";
    private String uri = "";
    private String prefix = null;
    private String affix = null;
    private String content = "";
    private Boolean embed = false;
    private Boolean async = false;
    private Boolean isFramework = false;
    private ArrayList<Res> children;

    public int fixCode() {
        int result = prefix != null ? prefix.hashCode() : 0;
        result = 31 * result + (affix != null ? affix.hashCode() : 0);
        return result;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getAffix() {
        return affix;
    }

    public void setAffix(String affix) {
        this.affix = affix;
    }

    public Boolean getEmbed() {
        return embed;
    }

    public void setEmbed(Boolean embed) {
        this.embed = embed;
    }

    public Boolean getAsync() {
        return async;
    }

    public void setAsync(Boolean async) {
        this.async = async;
    }

    public Boolean getIsFramework() {
        return isFramework;
    }

    public void setIsFramework(Boolean isFramework) {
        this.isFramework = isFramework;
    }

    public ArrayList<Res> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<Res> children) {
        this.children = children;
    }

}
