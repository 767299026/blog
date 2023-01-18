package blog.util;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class ResultUtil implements Serializable {

    private boolean success;

    private int code;

    private String msg;

    private Object data;


    public static ResultUtil success(Object data){
        return new ResultUtil(true,200,"success",data);
    }

    public static ResultUtil fail(int code, String msg){
        return new ResultUtil(false,code,msg,null);
    }

    public static ResultUtil fail(String msg) {
        return new ResultUtil(false,400, msg, null);
    }

    public static ResultUtil fail(int code,String msg,Object data){
        return new ResultUtil(false,code,msg,data);
    }
}
