package cn.com.leanvision.libbound.rx;

import android.util.Log;

import cn.com.leanvision.libbound.rx.busEvent.BusEvent;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;

/********************************
 * Created by lvshicheng on 2016/11/28.
 ********************************/
public class RxBus {

  private static final String TAG = RxBus.class.getSimpleName();

  private SerializedSubject<BusEvent, BusEvent> tPublishSubject;

  public static RxBus getInstance() {
    Log.e(TAG, InstanceBind.mRxBus.toString());
    return InstanceBind.mRxBus;
  }

  private RxBus() {
    tPublishSubject = new SerializedSubject<>(PublishSubject.<BusEvent>create());
  }

  public void postEvent(BusEvent event) {
    tPublishSubject.onNext(event);
  }

  public Observable<BusEvent> toObservable() {
    return tPublishSubject.asObservable().onBackpressureBuffer();
  }

  public boolean hasObservers() {
    return tPublishSubject.hasObservers();
  }

  private static class InstanceBind {
    static final RxBus mRxBus = new RxBus();
  }
}
