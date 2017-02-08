package clwang.chunyu.me.testdetailrxandroid;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * 更多的RxAndroid的使用方法.
 * <p>
 * Created by wangchenlong on 15/12/30.
 */
public class MoreActivity extends Activity {

    @Bind(R.id.simple_tv_text) TextView mTvText;

    final String[] mManyWords = {"Hello", "I", "am", "your", "friend", "Spike"};
    final List<String> mManyWordList = Arrays.asList(mManyWords);

    // Action1 类似订阅者,也就是观察者, 设置TextView
    // 接收到被观察者发送的数据（被订阅后）后，设置文本
    private Action1<String> mTextViewAction = new Action1<String>() {
        @Override public void call(String s) {
            mTvText.setText(s);
        }
    };

    // Action设置Toast
    // Action1 类似订阅者,也就是观察者,接收到被观察者发送的数据（被订阅后）后，弹出toast
    private Action1<String> mToastAction = new Action1<String>() {
        @Override public void call(String s) {
            Toast.makeText(MoreActivity.this, s, Toast.LENGTH_SHORT).show();
        }
    };

    // 设置映射函数
    //映射函数接口Func1，继承自 Function
    //将 List<String> 映射为 Observable<String> ，将集合映射为一个可以发送这个 集合 的的被观察者
    private Func1<List<String>, Observable<String>> mOneLetterFunc = new Func1<List<String>, Observable<String>>() {
        @Override public Observable<String> call(List<String> strings) {
            return Observable.from(strings); // 映射字符串
        }
    };

    // 设置大写字母
    //大写字母转换的映射函数
    private Func1<String, String> mUpperLetterFunc = new Func1<String, String>() {
        @Override public String call(String s) {
            return s.toUpperCase(); // 大小字母
        }
    };

    // 连接字符串
    //Func2 ，继承自Function ,将第二三个参数映射为第一个参数类型
    private Func2<String, String, String> mMergeStringFunc = new Func2<String, String, String>() {
        @Override public String call(String s, String s2) {
            return String.format("%s %s", s, s2); // 空格连接字符串
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);
        ButterKnife.bind(this);

        // 创建原始的被观察者observable，发送的数据是 Strting : "Hello, I am your friend, Spike!"
        // 添加字符串, 省略Action的其他方法, 只使用一个onNext.
        Observable<String> obShow = Observable.just(sayMyName());

        // 先映射, 再设置TextView
        //.observeOn(AndroidSchedulers.mainThread())，注册的观察者在主线程运行
        // .map(mUpperLetterFunc),对被观察者 obShow 发射的数据进行转换（小写转大写），得到新的被观察者对象
        obShow.observeOn(AndroidSchedulers.mainThread())
                .map(mUpperLetterFunc).subscribe(mTextViewAction);

        // 单独显示数组中的每个元素
        //创建被观察者 obMap，发送的是一连串的数据 ，是一个数组，会多次调用onNext()
        Observable<String> obMap = Observable.from(mManyWords);

        // 映射之后分发
        obMap.observeOn(AndroidSchedulers.mainThread())
                .map(mUpperLetterFunc).subscribe(mToastAction);

        // 优化过的代码, 直接获取数组, 再分发, 再合并, 再显示toast, Toast顺次执行.
        Observable.just(mManyWordList)
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(mOneLetterFunc)
                .reduce(mMergeStringFunc)
                .subscribe(mToastAction);

        /*
             .flatMap 与 map 一样，也是对被观察者发送的数据类型进行变换，不同的是
             map(new Fun(类型1，类型2))变换，得到一个新的被观察者，新的被观察者发送的数据就是 类型2
             .flatMap(new Fun(类型1，Observable<类型2>)) 变换，得到一个新的被观察者，新的被观察者发送的数据就是 类型2
             此处介绍的特别好：
             http://blog.csdn.net/new_abc/article/details/48025513
             Reduce操作符应用一个函数接收Observable发射的数据和函数的计算结果作为下次计算的参数，输出最后的结果。
                经过.flatMap(mOneLetterFunc) 后，observable发送的是一串 string
                reduce经过函数 mMergeStringFunc 将其聚合，怎么聚合，用空格连起来
              可参考：
              http://blog.csdn.net/pizza_lawson/article/details/45504039
        */


    }

    // 创建字符串
    private String sayMyName() {
        return "Hello, I am your friend, Spike!";
    }
}
