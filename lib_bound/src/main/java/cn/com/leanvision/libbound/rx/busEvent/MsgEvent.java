package cn.com.leanvision.libbound.rx.busEvent;

/********************************
 * Created by lvshicheng on 2016/11/29.
 ********************************/
public class MsgEvent implements BusEvent {

  public String type; // wifi

  public String msg;

  public MsgEvent(String msg) {
    this.msg = msg;
  }
}
