package biz.zacneubert.raspbert.getpodcast.RSS;

/**
 * Created by zacneubert on 9/18/15.
 */
public class RssEnclosure {
    private String qName;
    private String Value;
    private String URI;
    private String Type;

    public RssEnclosure(String qname, String value, String uri, String type) {
        qName = qname;
        Value = value;
        URI = uri;
        Type = type;
    }

    public String getQName() { return qName; }
    public String getValue() { return Value; }
    public String getURI() { return URI; }
    public String getType() { return Type; }

    @Override
    public String toString() {
        return Value;
    }
}
