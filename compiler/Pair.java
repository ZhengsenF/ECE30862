public class Pair {
    int offset;
    String dataType;
    public Pair(int in, String inString) {
        offset = in;
        dataType = inString;
    }

    public int getKey() {return offset;}
    public String getValue() {return dataType;}
}
