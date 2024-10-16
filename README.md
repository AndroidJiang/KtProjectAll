 


### **Android**

#### **Activity**

**dialog弹出和下拉通知栏生命周期会咋样，如果是dialog主题的act呢**

> 前者不变,后者会发生变化，但是不执行onStop

**为什么用bundle传递数据，不用hashmap。（bundle底层arraymap，android特有省内存，千以下的键值对用）**

>  bundle内部实现是ArrayMap，二分法查找，在小数据存储的时候，效率比Hashmap，而一般需要使用bundle的场景数据都比较小。hashmap是数组+链表，数据量小情况下，占用内存较多。一般act之间数据传递较少，故用bundle合适点。
>
>  bundle使用ParceLable序列化对象，而Hashmap是java的类，使用的是Serializable，效率上bundle高。

**在service等非activity中启动activity，为什么需要设置new_task**

> 如果你想要从一个非Activity上下文（如Service）中启动Activity，确实需要在Intent中设置FLAG_ACTIVITY_NEW_TASK标志。Service并没有自己的窗口，因此无法直接启动Activity，需要通过新的Task来承载该Activity。如果从Service中启动Activity而不设置这个标志，就会导致运行时异常，因为Service没有所属的Task，系统不知道该如何处理这个没有Task的Activity

**弹窗Dialog和Toast对当前生命周期有影响吗？**

> 没。只有AMS才会改变生命周期，这个是WMS，WindowManger.addView，而且Dialog和Toast都属于SubWindow，需要附着在Window之上才能显示，也就是Activity中的phoneWindow

 **启动Dialog主题或者透明主题的Activity对当前生命周期有影响吗？**

>  有。属于AMS，因为启动Activity是透明或者弹窗主题，所以原Activity只会执行OnPause方法，不会OnStop，还是“可见”状态。此场景下注意内存泄漏问题，在OnPause中执行停止ui更新逻辑，而不是OnStop或者Onstroy中，否则启动的Activity会泄漏（**动画一直执行，handler不空闲，导致此act该销毁的时候，没有空闲handler执行的机会，下面引用链不会赋值为空，从而产生泄露**）

 ![image-20220809213959039](https://raw.githubusercontent.com/AndroidJiang/HomePics/master/typora/image-20220809213959039.png)

```java
handleResumeActivity方法中
Looper.myQueue().addIdleHandler(new Idler());

Idler空闲，触发stop和destroy
```

**请介绍Activity的4种启动模式**

> 标准模式（standard）
>
> 栈顶复用模式（singleTop）
>
> 栈内复用模式（singleTask）
>
> 单例模式（singleInstance）

**切换横竖屏时 Activity的生命周期变化？**

> performLaunchActivity源码中有mInstrumentation顺序执行
>
> onSaveInstanceState-->onPause-->onStop-->onDestroy-->onCreate-->onStart-->onRestoreInstanceState-->onResume-->

 

**BroadCast**

**系统原理**

1. 广播接收者通过binder往AMS中注册

```java
networkIntentFilter = new IntentFilter();
networkIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
netChangeReceiver = new NetChangeReceiver();
registerReceiver(netChangeReceiver, networkIntentFilter);
```

2. 广播发送者通过binder向AMS发送

​		系统层监听到进行发送消息

3. AMS根据IntentFilter查找合适广播

4. AMS发送对应广播到消息列表中

5. 广播接受者通过消息循环拿到广播，回调onReceive

https://blog.csdn.net/luoshengyang/article/details/6730748

**本地广播原理**

```
// 注册接收器
BroadcastReceiver receiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        // 处理接收到的 intent 
        String action = intent.getAction();
        if("MY_ACTION".equals(action)){
            // TODO: 根据intent的不同执行不同的操作
        }
    }
};
LocalBroadcastManager.getInstance(context).registerReceiver(receiver, new IntentFilter("MY_ACTION"));

// 发送广播
Intent intent = new Intent("MY_ACTION");
LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

// 取消注册接收器
LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
```

1. 调用 sendBroadcast，传输广播 Intent，复用系统广播的bean

2. 利用 Intent 中的Action 索引广播数组列表，索引出广播实体。

3. 通过 Handler 回调到主线程，通过 executePendingBroadcasts 来运行广播。

```
rec.receiver.onReceive(mAppContext, br.intent);
```

注意：谷歌逐渐废弃本地广播，推荐使用`LiveData`，在ViewModel中存储数据的实例，Activity或Fragment可以观察这些数据并在数据变化时获得通知。这种方法更加灵活，它是生命周期感知的，可以防止内存泄漏和其他与生命周期相关的问题。尽管如此，`LocalBroadcastManager`仍可在一些老旧项目中使用。



**绍Android里广播的分类?**

> Android的广播主要分为两大类:
>
> - 标准广播:完全异步执行,广播发出后,接收器无法阻止广播的继续传播。
> - 有序广播:同步执行,广播按优先级顺序传递,优先级高的接收器可以阻断广播。

**程序A能否接收到程序B的广播?**

> 默认情况下,程序A无法接收到程序B发送的广播。想要接收,需要在广播发送端添加权限,并在接收端声明需要的相同权限,才能接收到跨程序的广播。

**列举广播注册的方式,并简单描述其区别?**

> 注册广播接收器主要有两种方式:
>
> - 动态注册:在代码中通过registerReceiver()方法注册,优点是可以选择合适的时机注册。
>
> - 静态注册:在AndroidManifest中通过<receiver>声明注册,优点是不需要在代码中注册。注意8.0后
>
>   为了不让所有app都胡乱的注册类似**开机广播**，再配合service实现应用不死，消耗系统资源，谷歌限制了只有系统级app才能接收到这些广播，如开机监听
>
>   ```java
>   android.intent.action.BOOT_COMPLETED
>   ```
>
>   **网络监听广播**，即使系统级别也不会接收到，必须以动态注册或者targetsdk小于26

**为什么不能够用回调的方式使用startActivityForResult呢**

> ```java
> startActivityForResult(intent, new CallBack() {
>    @Override
>    public void onActivityResult(int resultCode, Intent data) {
>    }
> });
> ```
>
> **匿名内部类的构造函数** 在上述的例子当中，callback是一个匿名内部类，我们都知道匿名内部类会持有外部类的引用
>
> **Activity被销毁的场景** 当A使用回调的方式跳转到B，此时由于某种原因A被销毁了（不造成内存泄漏），然后当B执行完成返回结果，系统会重新创建A1，而callback里面持有的是A引用，并不会对A1产生作用，这显然不是我们想要的结果。
>
> （当系统销毁一个 Activity 之后，如果用户导航回该 Activity（例如按下返回按钮），系统会尝试重新创建该 Activity，以便用户可以回到之前的状态。这是 Android 生命周期中的一部分，被称为“恢复”或“重建” Activity。）
>
> 如何解决：当activity被重建，我们可以通过反射，将新的activity重新set进去，这样callback引用的就是重建后的新的activity了。

 

 

**ContentProvider**

**简单实例：**

**进程A**

**1.** **创建数据库**

**2.** **继承ContentProvider，关联数据库，实现增删改查**

**3.** **配置文件中注册ContentProvider  exported=true**

**进程B**

**1.** **Uri访问provider，getContentResolver进行增删改查**





**问题:什么是内容提供者?**

> 内容提供者(ContentProvider)是Android不同应用之间实现数据共享的一种机制,通过内容URI来对数据进行操作,屏蔽不同程序之间的进程间通信细节。

**问题:ContentProvider、ContentResolver、ContentObserver之间的关系?**

> 它们之间的关系是:
>
> - ContentProvider:是真正的数据提供者,对外提供数据访问接口。
> - ContentResolver:外部应用通过它访问ContentProvider。
> - ContentObserver:观察ContentProvider的数据变化。

**问题:为什么要使用ContentProvider类进行交互,而不直接访问?**

> 使用ContentProvider的好处有:
>
> - 更高效的进程间通信方式
> - 提供了接口对外隐藏实现细节
> - 提高了数据访问的安全性
> - 统一了不同存储方式的数据访问接口

**问题:ContentProvider的底层采用Binder,为什么还需要ContentProvider?**

> 尽管ContentProvider底层也是采用Binder实现的进程间通信,但它提供了一整套内容共享的解决方案,包括权限控制、接口封装、数据操作等,使数据共享更加简单安全,这是单纯的Binder所不具备的。

延伸：三方sdk自动初始化



 

**View事件**

<img src="https://raw.githubusercontent.com/AndroidJiang/HomePics/master/typora/image128.png" alt="img" style="zoom:80%;" /> 

onTouch返回false，则不拦截，继续执行onTouchEvent，否则拦截

<img src="https://raw.githubusercontent.com/AndroidJiang/HomePics/master/typora/image129.png" alt="img" style="zoom:80%;" /> 

 

 

------

####  Fragment

**生命周期相关**

https://blog.csdn.net/u010687761/article/details/132635610

> **1:Activity如何通过生命周期去调用fragment的生命周期**
> 其实就是调用了fragmentManagerlmpl这个类来进行分发，发现最终都是走的dispatchStateChange(O;
> **2：发现fragment的生命周期状态只有5个**
> 状态机模式：通过降序以及升序来进行判断如果是升序，走显示的生命周期
> **3:发现case里面没有break。**这样的好处，是为了让fragment走完整的生命周期



**commit() ，commitAllowingStateLoss()， commitNow() ，commitNowAllowingStateLoss()区别**

> **commit() 方法**：
>
> - **场景**：通常在主线程中使用。
>
> - **用途**：`commit()` 方法将 Fragment 事务添加到 Activity 的主线程队列中，并在主线程的事件循环中执行。它适用于大多数情况，特别是当你不需要立即执行事务，而是希望将事务安排到主线程的队列中等待执行时。这样可以确保在合适的时机执行 Fragment 事务，避免在不适当的时候修改 Fragment 的状态。（handler发送消息）
>
>   **commitAllowingStateLoss()**：
>
>   - `commitAllowingStateLoss()` 方法与 `commit()` 方法类似，不同之处在于，即使在 Activity 的状态已经保存时，也允许提交事务。这意味着如果在 `commit()` 方法提交之后，Activity 被销毁或重新创建，但状态丢失的情况下，`commitAllowingStateLoss()` 仍然会执行事务。
>
> **commitNow() 方法**：
>
> - **场景**：适用于在主线程中立即执行 Fragment 事务的情况。
>
> - **用途**：`commitNow()` 方法会立即执行 Fragment 事务，而不是将其添加到主线程队列中等待执行。这在需要立即执行事务且你可以确保不会引起死锁或其他问题时非常有用。例如，当你知道当前没有任何可能阻塞主线程的操作时，可以使用 `commitNow()`。
>
>   **commitNowAllowingStateLoss()**：
>
>   - `commitNowAllowingStateLoss()` 方法与 `commitNow()` 方法类似，但允许在 Activity 状态已经丢失的情况下执行事务。同样，使用此方法也可能会导致应用程序状态的丢失或不一致，因此应该谨慎使用。

 

**setUserVisibleHint和onHiddenChanged触发条件**



案例：

首页GameArticleVideoFragment(`frag8`)中接受来自SettingActivity( `ActivityB` )自动播放开关的event，刷新数据并且更新adapter。
问题：返回到首页后（即使不在`frag8`，也会触发该页面的轮播）

**现象:**HomeActivity(`ActivityA`)中采用show/hide操作fragment，首页fragment(`frag1`)采用viewpager操作fragment，在 `ActivityB`中会触发event的接受，从 `ActivityB`返回到 `frag5`不会触发onBindViewHolder，但是到首页 `frag1` 会触发。

**分析:**

**返回到 `ActivityA`**：

- 用户从 `ActivityB` 返回后，`ActivityA` 默认显示 `frag5`。此时，`frag1` 及其内部的 `ViewPager` 仍然处于不可见状态。

**`EventBus` 通知 `frag8` 更新数据**：

- 在 `ActivityB` 中触发的 `EventBus` 消息可以成功通知到 `frag8`，并调用 `adapter.notifyDataSetChanged()` 来更新数据。
- 由于 `frag8` 的视图始终存在（因为设置了 `setOffscreenPageLimit`），所以数据会更新，但不可见时不会调用 `onBindViewHolder()`。

**手动点击 `frag1` 的 Tab**：

- 当用户手动点击 `frag1` 对应的 Tab，`fragment1` 会被显示出来，视图也会变为可见。
- 在这个过程中，由于 `frag1` 中的 `ViewPager` 缓存了所有页面，包括 `frag8`，因此 `frag8` 的 `RecyclerView` 视图已经准备好。
- 如果此时数据已经通过 `notifyDataSetChanged()` 更新，`onBindViewHolder()` 将会被调用，以便将新的数据绑定到视图。











---

#### Service

<img src="https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20220926163221237.png" alt="image-20220926163221237" style="zoom:50%;" />



**Android8.0 startService有啥问题**

> app处于后台，startService会抛异常 
> https://juejin.cn/post/6844903859710754823
>
> 简单的使用可以查看https://blog.csdn.net/weixin_42602900/article/details/126704070
>
> 注意点：1 高版本开启前台服务；2 高版本通知设置渠道id



```kotlin
public class MyService extends Service {
	private NotificationManager notificationManager;
	private String notificationId = "channelId";
	private String notificationName = "channelName";
	、、、
	private Notification getNotification() {
    Notification.Builder builder = new Notification.Builder(this)
            .setSmallIcon(R.drawable.logo_small)
            .setContentTitle("测试服务")
            .setContentText("我正在运行");
    //设置Notification的ChannelID,否则不能正常显示
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        builder.setChannelId(notificationId);
    }
    Notification notification = builder.build();
    return notification;
}
@Override
public void onCreate() {
    super.onCreate();
    notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    //创建NotificationChannel
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
        NotificationChannel channel = new NotificationChannel(notificationId, notificationName, NotificationManager.IMPORTANCE_HIGH);
        notificationManager.createNotificationChannel(channel);
    }
    startForeground(1,getNotification());//前台服务5s内开创建前台通知
}
```

**如何解决8.0上启动service的问题？**

> 1.判断高于8.0 ，前台启动
>
> ```kotlin
> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
> 	activity.startForegroundService(intent);
> } else {
> 	activity.startService(intent);
> }
> ```
>
> 但是前台启动服务必须在5s内调用startForeground显示通知栏，如果不想要咋办？在此服务中startForegroundService另一个服务，这个服务和它公用Notification ID（然后立马stopSelf，destroy中进行移除通知栏）
>
> 2.上面代码，如果还会崩溃，详见下面链接，可以采用JobIntentService
>
> https://www.freesion.com/article/49371161948/

**总结**

- **startService抛异常不是看调用的APP处于何种状态，而是看Servic所在APP处于何种状态，因为看的是UID的状态，所以这里重要的是APP而不仅仅是进程状态**
- 不要通过Handler延迟太久再startService，否则可能会有问题
- 应用进入后台，60s之后就会变成idle状态，无法start其中的Service，但是可以通过startForegroundService来启动
- Application里面不要startService，否则恢复的时候可能有问题
- startForeGround 要及时配合startForegroundService，否则会有各种异常。(说反了吧？)
- 采用JobIntentService(待研究)



面试题：

- **问题:请介绍Service的启动方式,启动方式的区别?**

  > Service可以通过startService()和bindService()两种方式启动,主要区别是:
  >
  > - startService():启动服务,服务会在后台无限期运行,直到调用stopService()或自身的stopSelf()方法停止。服务停止后可再次启动。
  > - bindService():绑定服务,与组件之间建立连接。需要调用unbindService()断开连接,服务才会被销毁。绑定服务更适合需要与调用方交互的服务。

  **问题:请介绍Service的生命周期?**

  > Service的生命周期包含以下方法:
  >
  > - onCreate():服务第一次创建时调用,可在此做一些初始化工作。
  > - onStartCommand():每次启动服务时调用。重t写此方法可自定义启动行为。
  > - onDestroy():服务销毁前调用,可在此处理收尾工作。
  > - onBind():客户端绑定服务时调用。可在此返回IBinder接口实例,与客户端交互。

  **问题:Activity、Service、intent之间的联系?**

  > Activity和Service之间主要通过Intent来交互:
  >
  > - Activity可以通过startService()和bindService()来启动Service。
  > - Service通过Intent可以将执行的结果返回给启动它的Activity。
  > - Activity通过unbindService()来断开与Service的连接等。

  **问题:在Activity和Service中创建Thread的区别?**

  > Activity和Service中创建的Thread主要区别:
  >
  > - Activity中的Thread会随着Activity的销毁而销毁。
  > - 而Service中的Thread不受Activity生命周期影响,会持续运行直到Service被销毁。
  > - 因此Service更适合运行持续性任务、后台任务等。





---



 

 

#### **Handler**

 **Message**基于**单链表**实现

**1.** **内存泄漏**（匿名内部类持有外部类引用）

**2.** **流程图**（见下）

**3.** **ThreadLocal**(见上专题)

**4.** **msg.next() 中 nativePollOnce(ptr,time) time为阻塞时间（距离下一条消息）**

补充：pipe机制，在没有消息时阻塞线程并进入休眠释放cpu资源，有消息时唤醒线程

Linux pipe/epoll机制，简单说就是在主线程的MessageQueue没有消息时，便阻塞在loop的queue.next()中的nativePollOnce() 方法里



**面试题**

**阻塞了为什么还能继续响应用户操作呢**？

当系统收到来自因用户操作而产生的通知时, 会通过 Binder 方式从系统进程跨进程的通知我们的 application 进程中的 ApplicationThread,ApplicationThread又通过 Handler 机制往主线程的 messageQueue中插入消息，从而让主线程的loop()，Message msg = queue.next()这句代码可捕获一条 message ,然后通过 msg.target.dispatchMessage(msg)来处理消息,从而实现了整个 Android 程序能够响应用户交互和回调生命周期方法（具体实现ActivityThread 中的内部类H中有实现）

**为什么当主线程loop()处于死循环不会卡死**

1. 首先我们不要弄混了消息等待阻塞(休眠)和消息处理阻塞，消息等待阻塞会导致主线程进入休眠，不会ANR，而消息处理阻塞会导致ANR。
2. 休眠不会阻碍主线程对新消息的感知和处理(有消息会被唤醒)，但消息处理阻塞会，现在你们明白系统为什么要给主线程设置5秒的超时限制了吧。
3. 当没有消息时，queue.next()会阻塞在nativePollOnce(),这里涉及到Linux pipe/epoll机制,nativePollOnce()被阻塞时,主线程会释放CPU资源,进入休眠状态。如果此时来了新消息，比如vSync、用户操作、系统广播等，主线程会从休眠中唤醒，处理完新消息后，再次queue.next()进入休眠，如此反复。
4. 总体来说该机制是为了节省和充分利用CPU资源。

**那么我们在主线程中耗时为什么会造成 ANR 异常呢?** 

那是因为我们在主线程中进行耗时的操作是属于在这个死循环的执行过程中, 如果我们进行耗时操作, 可能会导致这条消息还未处理完成,后面有接受到了很多条消息的堆积,从而导致了 ANR 异常.

**一个Thread可以有几个Looper？几个Handler？**

一个线程只能有一个Looper，可以有多个Handler，在线程中我们需要调用Looper.perpare,他会创建一个Looper并且将Looper保存在ThreadLocal中，每个线程都有一个LocalThreadMap，会将Looper保存在对应线程中的LocalThreadMap，key为ThreadLocal，value为Looper

<img src="https://raw.githubusercontent.com/AndroidJiang/HomePics/master/typora/image130.png" alt="img" style="zoom:50%;" /> 

**handler内存泄漏如何解决**

- 静态内部类+弱引用（**两者搭配使用**，如果只有静态，用到了外部类中的资源，如果直接传递对象，还是会造成泄露）

  ```java
  private static class AppHandler extends Handler {
      //弱引用，在垃圾回收时，被回收
      WeakReference<Activity> activity;
      AppHandler(Activity activity){
          this.activity=new WeakReference<Activity>(activity);
      }
      public void handleMessage(Message message){
          		MyActivity activity = activityReference.get();
              if (activity != null) {//注意判空	
                  activity.updateUI(); // Activity 还没被回收时才操作
              }
      }
  }
  ```

  

- removeCallbacksAndMessages

- WeakHandler（缺点：可能会被回收，不执行内部延迟消息）

**引用链**（jvm可达性分析）

主线程 （持有）—> threadlocal —> Looper —> MessageQueue —> Message —> Handler —> Activity

**message是如何重复利用的（池化思想-对象优化）**----享元模式

```java
public static Message obtain() {
    synchronized (sPoolSync) {
        if (sPool != null) {
            Message m = sPool;
            sPool = m.next;//当前栈元素赋值为当前栈顶元素的next，即栈顶“指针”往下移一个单位
            m.next = null;// 旧栈顶元素/获取的对象/出栈的元素的next置为空，即链表关系断开，出栈操作完成
            m.flags = 0; // clear in-use flag
            sPoolSize--;
            return m;
        }
    }
    return new Message();
}
```

1. synchronized (sPoolSync)：给对象加锁，保证同一时刻只有一个线程使用Message。
2. if (sPool != null)：判断sPool链表是否是空链表，如果是空，就直接创建一个Message对象返回；否则就进入第三步。
3. 链表操作：将链表头节点移除作为重用的Message对象，第二个节点作为新链表（sPool ）的头节点。

**回收消息**

1. 清空消息的内容（成员对象变量置空，基础类型变量置为零，状态恢复默认值等）
2. 缓存的消息对象入栈（next引用赋值为当前栈顶，当前栈顶赋值为当前缓存的消息对象），缓存元素大小加1

```
void recycleUnchecked() {
	next = sPool;
	//栈顶元素替换为当前入缓存的消息对象
	sPool = this;
	sPoolSize++;
}
```



**handler为什么可以切换线程**

内存共享

> 1. 当在A线程中创建handler的时候，Looper.prepare创建looper和messageQueue
> 2. 在B线程向A线程发消息，这个handler 是在A线程初始化的，意思是用了A线程的Looper.loop(),Looper 在A线程中调用loop 进入一个无限的for 循环从MessageQueue 中取消息,子线程调用mMainHandler 发送一个message
> 3. 通过msg.target.dispatchMessage(msg) 将message 插入到mMainHandler 对应的MessageQueue 中Looper 发现有message 插入到MessageQueue 中，便取出message 执行相应的逻辑
> 4. 因为Looper.loop()是在A线程中启动的，所以则回到了A线程中，达到了切换线程的目的

⑨子线程中创建handler

```java
Looper.prepare();
new Handler()-------(mLooper = Looper.myLooper();
        						if (mLooper == null) {
            				throw new RuntimeException(
                			"Can't create handler inside thread " + Thread.currentThread()
                        + " that has not called Looper.prepare()");    )   		
Looper.loop();
```

**源码分析**

```java
#Looper
 private static void prepare(boolean quitAllowed) {
        if (sThreadLocal.get() != null) {
            throw new RuntimeException("Only one Looper may be created per thread");
        }
        sThreadLocal.set(new Looper(quitAllowed));//threadLocal设置当前线程looper
}
private Looper(boolean quitAllowed) {
    mQueue = new MessageQueue(quitAllowed);//创建MessageQueue
    mThread = Thread.currentThread();
}
```

```java
public static void loop() {
    .....
    for (;;) {
        // 无限取消息，“might block” 指的就是nativePollOnce的阻塞
        Message msg = queue.next(); // might block
        if (msg == null) {
            // 如果返回的消息是空的，则会退出循环。如果循环没退出并且没有消息，则会被nativePollOnce阻塞着。
            return;
        }
        // 分发消息，target即是发送消息的Handler
        msg.target.dispatchMessage(msg);
        ......
        // 回收消息 变量清空 方便复用
        msg.recycleUnchecked();
}
```

```java
MessageQueue#next()
for (;;) {
// 阻塞，nextPollTimeoutMillis为等待时间，如果为-1则会一直阻塞
    nativePollOnce(ptr, nextPollTimeoutMillis);
    synchronized (this) {
        // 获取时间，还是通过uptimeMillis这个方法
        final long now = SystemClock.uptimeMillis();
        Message prevMsg = null;
        Message msg = mMessages;
        // 如果队列头部消息为屏障消息，即“target”为空的消息，则去寻找队列中的异步消息
        if (msg != null && msg.target == null) {
            // Stalled by a barrier.  Find the next asynchronous message in the queue.
            do {
                prevMsg = msg;
                msg = msg.next;
            } while (msg != null && !msg.isAsynchronous());
        }  
        // 如果队列头部消息不是屏障消息，就会直接处理
        // 如果是，就会获取异步消息，获取的到就处理，获取不到就去运行省略的代码
        if (msg != null) {
            if (now < msg.when) {
                // 当前时间小于消息的时间，设置进入下次循环后的等待时间
                // Next message is not ready.  Set a timeout to wake up when it is ready.
                nextPollTimeoutMillis = (int) Math.min(msg.when - now, Integer.MAX_VALUE);
            } else {
                // Got a message.
                // 一个标记，表示next循环是不是还在被阻塞着
                mBlocked = false;
                // 移除消息
                if (prevMsg != null) {
                    // 移除异步消息
                    prevMsg.next = msg.next;
                } else {
                    // 移除同步消息
                    mMessages = msg.next;
                }
                msg.next = null;              
                // 标记为正在使用
                msg.markInUse();
                return msg;
            }
        } else {
            // 没有获取到消息，接下来运行下面省略的代码，nextPollTimeoutMillis为“-1”，在循环开始的nativePollOnce方法将会一直阻塞。
            nextPollTimeoutMillis = -1;
        } 
}
```

```java
public void dispatchMessage(Message msg) {
    if (msg.callback != null) {
        handleCallback(msg);
    } else {
        if (mCallback != null) {
            if (mCallback.handleMessage(msg)) {
                return;
            }
        }
        handleMessage(msg);
    }
}
```

**发送消息**

```csharp
boolean enqueueMessage(Message msg, long when) {
            //插入前先消息队列是否有消息，新的头，如果被阻止，则唤醒事件队列。
            if (p == null || when == 0 || when < p.when) {
                //将消息放进队列头部
                msg.next = p;
                mMessages = msg;
                needWake = mBlocked;//指示next（）是否被阻止在pollOnce（）中以非零超时等待。如果阻塞，则需要唤醒looper
            } else {
                /*插入队列中间。 通常，除非队列的开头有障碍并且消息是队列中最早的
                  异步消息，否则我们不必唤醒事件队列。（队列中消息不为空，并且next（）也没有阻塞）*/
                needWake = mBlocked && p.target == null && msg.isAsynchronous();
                Message prev;
                for (;;) {
                    prev = p;
                    p = p.next;
                    if (p == null || when < p.when) {
                        break;
                    }
                    if (needWake && p.isAsynchronous()) {
                        needWake = false;
                    }
                }
                msg.next = p; // invariant: p == prev.next
                prev.next = msg;
            }
            // 如果looper阻塞/休眠中，则唤醒looper循环机制处理消息
            if (needWake) {
                nativeWake(mPtr);//唤醒
            }
        }
        return true;
    }
```

**为什么不用wait和notify？**

> 单纯用wait和notify，只能处理java层的消息，对于系统的消息不能处理。





**HandlerThread**

HandlerThread继承了Thread,是一种可以使用Handler的Thread；在run方法中通过looper.prepare()来开启消息循环，这样就可以在HandlerThread中创建Handler了；外界可以通过一个Handler的消息方式来通知HandlerThread来执行具体任务；确定不使用之后，可以通过quit或quitSafely方法来终止线程执行；具体使用场景是IntentService

```java
@Override
public void run() {
    mTid = Process.myTid();
    Looper.prepare();
    synchronized (this) {//参考下面IntentService代码使用，getLooper时，加锁如果是空，则wait释放锁等待，因为looper的创建在子线程慢，所以等待，然后notifyAll，但是此时还没释放锁，等整个同步代码执行完才会释放锁
        mLooper = Looper.myLooper();//
        notifyAll();
    }
    Process.setThreadPriority(mPriority);
    onLooperPrepared();
    Looper.loop();
    mTid = -1;
}
 public Looper getLooper() {
        synchronized (this) {
            while (isAlive() && mLooper == null) {
                try {
                    wait();
                }
            }
        }
        return mLooper;
    }
```

**考点：**

**notify  notifyAll  wait**

 

**IntentService**

onCreate:创建HandlerThread，拿到对应looper，创建handler

```java
@Override
public void onCreate() {
    super.onCreate();
    HandlerThread thread = new HandlerThread("IntentService[" + mName + "]");
    thread.start();
    mServiceLooper = thread.getLooper();
    mServiceHandler = new ServiceHandler(mServiceLooper);//子线程looper
}
```

onStartCommand->onStart  handler发送消息，处理onHanldeIntent，stopSelf

```java
@Override
public void onStart(@Nullable Intent intent, int startId) {
    Message msg = mServiceHandler.obtainMessage();
    msg.arg1 = startId;
    msg.obj = intent;
    mServiceHandler.sendMessage(msg);
}

private final class ServiceHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            onHandleIntent((Intent)msg.obj);
            stopSelf(msg.arg1);
        }
}
```

使用：

```java
/**
 * 这个IntentService虽然废弃，但是它的出现主要是避免线程的冲突，
 * 有些时候在使用Service的时候启动一个new Thread ，到头没stop掉就会持续占据，浪费资源
 */
public class MyIntentService extends IntentService {
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //这里自动启动一个线程进行操作，不会影响到主线程
        Log.d("MyIntentService","Thread String :" + Thread.currentThread().getId());//打印线程id就会发现和主线程是不一样的。
    }
}
```

 **IntentService与Service的区别**

从属性 & 作用上来说 Service：依赖于应用程序的主线程（不是独立的线程）

不建议在Service中编写耗时的逻辑和操作，否则会引起ANR；

IntentService：创建一个工作线程来处理多线程任务 　　

Service需要主动调用stopSelft()来结束服务，而IntentService不需要（在所有intent被处理完后，系统会自动关闭服务）



**JobIntentService**





---



#### SP



![image-20230810094831381](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20230810094831381.png)

**初始化**（子线程读取文件到map）

```java
SharedPreferences sharedPreferences = context.getSharedPreferences("spName", Context.MODE_PRIVATE);
```

```java
ContextImpl（Context实现类）
public SharedPreferences getSharedPreferences(String name, int mode) {
    File file;
    synchronized (ContextImpl.class) {
        if (mSharedPrefsPaths == null) {
            mSharedPrefsPaths = new ArrayMap<>();
        }
        file = mSharedPrefsPaths.get(name);
        if (file == null) {
            file = getSharedPreferencesPath(name);
            mSharedPrefsPaths.put(name, file);
        }
    }
    return getSharedPreferences(file, mode);
}
public SharedPreferences getSharedPreferences(File file, int mode) {
        SharedPreferencesImpl sp;
        final ArrayMap<File, SharedPreferencesImpl> cache = getSharedPreferencesCacheLocked();
            sp = cache.get(file);
            if (sp == null) {
                sp = new SharedPreferencesImpl(file, mode);
                cache.put(file, sp);
                return sp;
            }
        }
        ...
        // 最终返回SharedPreferencesImpl，SharedPreferencesImpl 实现了SharedPreferences的接口
        return sp;
    }
```

```java
SharedPreferencesImpl(File file, int mode) {
    startLoadFromDisk();
}
private void startLoadFromDisk() {
    new Thread("SharedPreferencesImpl-load") {
        public void run() {
            loadFromDisk();
        }
    }.start();
}
private void loadFromDisk() {
    Map<String, Object> map = null;
    try {
        stat = Os.stat(mFile.getPath());
        if (mFile.canRead()) {
            BufferedInputStream str = null;
            try {
                str = new BufferedInputStream(
                        new FileInputStream(mFile), 16 * 1024);
                // 最终就是通过这个方法把上面得到的file文件的内容撸到map中
                map = (Map<String, Object>) XmlUtils.readMapXml(str);
            } 
        }
    } 
}
```

初始化会生成<File, SharedPreferencesImpl>的cache，构造中调用startLoadFromDisk()，startLoadFromDisk开了一个线程异步调用loadFromDisk()，loadFromDisk最终通过XmlUtils.readMapXml把之前得到的[储存sp的xml的值]撸到map中（有点拗口），sp的初始化就完成了

**get**

```java
sp = context.getSharedPreferences(space, 0);
sp.getInt();
public int getInt(String key, int defValue) {
    synchronized (mLock) {
        awaitLoadedLocked();
        Integer v = (Integer)mMap.get(key);
        return v != null ? v : defValue;
    }
}
```

> 只有首次getInt时才会读取磁盘，后续只会从map中直接读取，代码在getSharedPreferences中，sp = cache.get(file);sp为null则
>
> 从磁盘读取

**put**（操作map）

```java
SharedPreferences sp = context.getSharedPrefens("spName",Context.MODE_PRIVATE);
SharedPreferences.Editor editor = sp.edit(); //editor操作内存mModified
editor.putString(key, value);
```

继续来看putString的源码，代码是在SharedPreferencesImpl的EditorImpl中。

```java
public final class EditorImpl implements Editor {
    private final Object mEditorLock = new Object();
    private final Map<String, Object> mModified = new HashMap<>();
    @Override
    public Editor putString(String key, @Nullable String value) {
        synchronized (mEditorLock) {
            mModified.put(key, value);
            return this;
        }
    }
```

由此发现sp文件保存ArrayMap，具体键值对保存HashMap

**commit**(同步主线程，有结果返回)

> 1 文件读写操作入队列，将会在子线程中操作文件读写。
> 2 CountDownLatch countDownLatch = new CountDownLatch(1)，使用countDownLatch.await()来阻塞主线程执行
> 3 文件读写完成后，调用countDownLatch.countDown()，恢复线程

```java
	@Override
	public boolean commit() {
	    // 通过调用commitToMemory方法返回一个MemoryCommitResult对象，对象的定义写在下面。
	    MemoryCommitResult mcr = commitToMemory();//将editor中mModified塞給sp中的内存map，方便直接从内存get
			// 然后enQueue到一个链表中，将值写入到文件中（开子线程），写入file的代码后面再看
	    SharedPreferencesImpl.this.enqueueDiskWrite(mcr, null);
	    try {
	    // 重点是这一句，会阻塞线程
	        mcr.writtenToDiskLatch.await();
	    } 
	    notifyListeners(mcr);//理解为这个地方有个CountDownLatch，结束了才会执行下面return
	    return mcr.writeToDiskResult;
	}

	private static class MemoryCommitResult {
	    // 重点关注下这个东西：CountDownLatch，先记住它初始化给的参数是1，在代码下面解读
	    final CountDownLatch writtenToDiskLatch = new CountDownLatch(1);
	    void setDiskWriteResult(boolean wasWritten, boolean result) {
	        // 操作成功的回调
	        writtenToDiskLatch.countDown();
	    }
	}
   private MemoryCommitResult commitToMemory() {
       // 定义一个map，用来保存即将写到file中的kv
       Map<String, Object> mapToWriteToDisk;
       synchronized (SharedPreferencesImpl.this.mLock) {
        ...
        }
       return new MemoryCommitResult(memoryStateGeneration, keysModified, listeners,
               mapToWriteToDisk);
   }
```

写文件

```java
private void enqueueDiskWrite(final MemoryCommitResult mcr,
                                  final Runnable postWriteRunnable) {
        final boolean isFromSyncCommit = (postWriteRunnable == null);
        final Runnable writeToDiskRunnable = new Runnable() {
                @Override
                public void run() {
                    synchronized (mWritingToDiskLock) {
                    	// 先执行 写文件操作，
                        writeToFile(mcr, isFromSyncCommit);
                    }
                }
            };
        if (isFromSyncCommit) {
        	boolean wasEmpty = false;
        	synchronized (SharedPreferencesImpl.this) {
            // 如果此时只有一个 commit 请求（注意，是 commit 请求，而不是 apply ）未处理，那么 wasEmpty 为 true，如果不等于1，那么也会和apply一样走下面队列work
            wasEmpty = mDiskWritesInFlight == 1;
        	} 
        	if (wasEmpty) {
            	// 当只有一个 commit 请求未处理，那么无需开启线程进行处理，直接在本线程执行 writeToDiskRunnable 即可（当前是主线程，那么直接主线程操作）
            	 writeToDiskRunnable.run();
           		 return;
        		}
    		}
        QueuedWork.queue(writeToDiskRunnable, !isFromSyncCommit);//apply会走
    }
```

![image-20230809171115579](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20230809171115579.png)



**apply**（异步子线程，无结果返回）

```java
public void apply() {
    // 将 mModified 保存的写记录同步到内存中的 mMap 中，并且返回一个 MemoryCommitResult 对象
    final MemoryCommitResult mcr = commitToMemory(); 
    final Runnable awaitCommit = new Runnable() {
        public void run() {
            try {
                mcr.writtenToDiskLatch.await();
            } catch (InterruptedException ignored) {
            }
        }
    };
    QueuedWork.add(awaitCommit);
    Runnable postWriteRunnable = new Runnable() {
        public void run() {
            awaitCommit.run();
            QueuedWork.remove(awaitCommit);
        }
    };
    // 将数据落地到磁盘上，注意，传入的 postWriteRunnable 参数不为 null，所以在
    // enqueueDiskWrite 方法中会开启子线程异步将数据写入到磁盘中
    SharedPreferencesImpl.this.enqueueDiskWrite(mcr, postWriteRunnable);
    notifyListeners(mcr);
}  
```

总结一下`apply()`方法：

- `commitToMemory()`方法将`mModified`中记录的写操作同步回写到内存 `SharedPreferences.mMap` 中。此时, 任何的`getXxx`方法都可以获取到最新数据了
- 通过`enqueueDiskWrite`调用`writeToFile`将所有数据**异步**写入到磁盘中

流程图参考如上不为1的情况。



**commit与apply区别：**

- `commit()` 是直接同步地提交到硬件磁盘，因此，多个并发的采用 `commit()` 做提交的时候，它们会等待正在处理的 `commit()` 保存到磁盘后再进行操作，从而降低了效率。而 `apply()` 只是原子的提交到内容，后面再调用 `apply()` 的函数进行异步操作。
- 翻源码可以发现 `apply()` 返回值为 void，而 `commit()` 返回一个 boolean 值代表是否提交成功。
- `apply()` 方法不会有任何失败的提示。

**那到底使用 commit() 还是 apply()？**

大多数情况下，我们都是在同一个进程中，这时候的 `SharedPrefrence` 都是单实例，一般不会出现并发冲突，如果对提交的结果不关心的话，我们非常建议用 `apply()` ，当然需要确保操作成功且有后续操作的话，还是需要用 `commit()` 的。



sp面试题：

- `SharedPreferences`是如何保证线程安全的，其内部的实现用到了哪些锁？

  三把锁：sp的map，editor的map，apply时合并

  ```kotlin
  	@GuardedBy("mLock")
    private Map<String, Object> mMap;
    @GuardedBy("mWritingToDiskLock")
    private long mDiskStateGeneration;
    public final class EditorImpl implements Editor {
      @GuardedBy("mEditorLock")
      private final Map<String, Object> mModified = new HashMap<>();
    }
  ```

- **editor.put后执行同步commit或者apply异步后，get的值永远都是最新的吗？**

  > commit:同步操作，肯定是最新的
  >
  > apply：异步操作，但是会立即写入值到sp的map中，写入磁盘逻辑是在其他线程。大部分场景get是最新的，
  >
  > 两种场景不是最新：多进程：apply时有其他进程首次获取get，会读取磁盘，这会儿可能获取的是旧值。
  >
  > ​								  多线程：多线程put也就是并发下，不能保证最终data数据

- 进程不安全是否会导致数据丢失？

- 数据丢失时，其最终的屏障——文件备份机制是如何实现的？

- 如何实现进程安全的`SharedPreferences`？

- sp为啥会发生anr

  

https://juejin.cn/post/6884505736836022280





**Sp的痛点**

- SP第一次加载数据时需要全量加载，当数据量大时可能会阻塞UI线程造成卡顿

- SP读写文件不是类型安全的，且没有发出错误信号的机制，缺少事务性API

- commit() / apply()操作可能会造成ANR问题：

  commit()是同步提交，会在UI主线程中直接执行IO操作，当写入操作耗时比较长时就会导致UI线程被阻塞，进而产生ANR；apply()虽然是异步提交，但异步写入磁盘时，如果执行了Activity / Service中的onStop()方法，那么一样会同步等待SP写入完毕，等待时间过长时也会引起ANR问题。

- 不支持多进程

- 全量更新



![image-20230810095821684](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20230810095821684.png)

mmap

![image-20230810100314426](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20230810100314426.png)

mmap是linux的api，用ndk去调用

![image-20230810102248144](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20230810102248144.png)

mmap映射大小可以设置，一页（4096字节）的整数倍，那为什么binder是1M-8k？

> 因为binder自己做了限制，也可以设置2M-8k，但是可能考虑到用的比较小，性能问题等

**MMAP优势**

1、MMAP对文件的读写操作只需要从磁盘到用户主存的一次数据拷贝过程，减少了数据的拷贝次数，提高了文件读写效率。

2、MMAP使用逻辑内存对磁盘文件进行映射，操作内存就相当于操作文件，不需要开启线程，操作MMAP的速度和操作内存的速度一样快。

3、MMAP提供一段可供随时写入的内存块，App 只管往里面写数据，由操作系统如内存不足、进程退出等时候负责将内存回写到文件，不必担心 crash 导致数据丢失。





**MMKV**

**更新方式**

1. 增量写入：不管key是否重复，直接将数据追加在前数据后。这样效率更高，更新数据只需要插入一条数据即可。（当然这样也会带来问题，如果不断增量追加内容，文件越来越大，怎么办？）
2. 全量写入：将数据去掉重复key后，如果文件大小满足写入的数据大小，则可以直接更新全量写入，否则需要扩容。（在扩容时根据平均每个K-V大小计算未来可能需要的文件大小进行扩容，防止经常性的全量写入）

**MMKV优点**

1. MMKV实现了SharedPreferences接口，可以无缝切换。
2. 通过 mmap 内存映射文件，提供一段可供随时写入的内存块，App 只管往里面写数据，由操作系统负责将内存回写到文件，不必担心 crash 导致数据丢失。
3. MMKV数据序列化方面选用 protobuf 协议，pb 在性能和空间占用上都有不错的表现。
4. SP是全量更新，MMKV是增量更新，有性能优势。
5. 支持多进程，文件锁 flock



------



#### Binder

**定义：**

在Android中我们所使用的Activity，Service等组件都需要和AMS(system_server) 

通信，这种跨进程的通信都是通过Binder完成。 

-  机制：Binder是一种进程间通信机制； 
-  驱动：Binder是一个虚拟物理设备驱动； 
-  应用层：Binder是一个能发起通信的Java类；

**多进程优势：**

虚拟机分配给各个进程的运行内存是有限制的，LMK也会优先回收对 

系统资源的占用多的进程。 

-  突破进程内存限制，如图库占用内存过多； 
-  功能稳定性：独立的通信进程保持长连接的稳定性； 
-  规避系统内存泄漏: 独立的WebView进程阻隔内存泄露导致的问题； 
-  隔离风险：对于不稳定的功能放入独立进程，避免导致主进程崩溃；



**饭前甜点**：先复习下**mmap**，两个activity在不同的进程，之间进行通信<img src="https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20240111220053492.png" alt="image-20240111220053492" style="zoom: 80%;" />

<img src="https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20240111215943270.png" alt="image-20240111215943270" style="zoom:80%;" />

<img src="https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20240111215859684.png" alt="image-20240111215859684" style="zoom:80%;" />

主要就是mmap进行物理地址和内存地址之间的映射，无需拷贝，那么binder的一次拷贝是在哪里呢？

> 一次拷贝发生在c端，而s端则跟上面原理一样，进行了mmap映射，详见下面





**ipc对比**(车机必面)

<img src="https://raw.githubusercontent.com/AndroidJiang/HomePics/master/typora/image-20220802210943550.png" alt="image-20220802210943550" style="zoom:60%;" />

实名服务：AMS 、 WMS（系统服务）

匿名服务：一般自己的服务

区别是：是否在ServiceManager中注册，如果注册实名，否则匿名

**aidl流程**

https://mp.weixin.qq.com/s/S7lGxGZApgqFTuRCMIKhJA码牛<img src="https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20240111223331175.png" alt="image-20240111223331175" style="zoom: 67%;" /><img src="https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20240111223455071.png" alt="image-20240111223455071" style="zoom:80%;" /><img src="https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20240113193259018.png" alt="image-20240113193259018" style="zoom:80%;" />



疑问：为什么需要配合service？

疑问：往服务端发送数据为什么那样写？

![image-20220804212305034](https://raw.githubusercontent.com/AndroidJiang/HomePics/master/typora/image-20220804212305034.png)

**binder原理**

![binder原理](https://raw.githubusercontent.com/AndroidJiang/HomePics/master/typora/binder%E5%8E%9F%E7%90%86.png)

用户态切换内核态，上下文切换，类似多个线程之间的切换，会保存当前运行的状态，比较耗时耗资源

跨进程socket传递数据过程：进程1用户空间copy到内核空间，因为内核共享，所以进程2可以直接copy到自己的用户空间，两次拷贝



> mmap：Linux通过将一个虚拟内存区域(用户空间)与一个磁盘上的对象(文件)关联起来，以初始化这个虚拟内存区域的内容，这个过 程称为内存映射(memory mapping)。联想：**MMKV**

Binder传递数据过程：少了一次拷贝，直接将进程2的用户空间中一块区域mmap映射到内核空间中,如下。

<img src="https://raw.githubusercontent.com/AndroidJiang/HomePics/master/typora/image-20220802214942593.png" alt="image-20220802214942593" style="zoom:67%;" />

**为什么需要Binder机制**

> ipc有管道，socket，信号量，共享空间等
>
> **性能角度** 管道 消息队列 套接字都需要两次数据拷贝，共享内存方式一次拷贝都不需要，Binder也只需要拷贝一次，其性能仅次于共享内存稳定性角度 Binder基于CS架构，Server与Client断相对独立，稳定性较好。共享内存实现方式复杂且没有客户与服务端之别，需要充分考虑到访问临界并发同步问题。
>
> **安全角度**传统的IPC对于通信双方的身份没有做出严格的验证，使用传统IPC机制，只能由客户在数据包内填入UID/PID,不可靠.Android为每个安装好的应用程序分配了自己的UID，故进程的UID是鉴别进程身份的重要标志，Android系统只向外暴露Client端，Client端将任务发送给Server端，根据权限控制策略，判断PID与UID是否符合权限。

**疑问：那么为什么发送方不进行mmap呢？**

> 因为如果发送方也进行mmap，那就是共享内存的方式了，性能好了，但是使用起来很复杂，死锁数据不同步等问题。

**intent传递数据大小**

> mmap  内存映射  1M-8k，其实到不了那么多，预留打包

**谈谈你对 binder 的理解**

> binder 是 Android 中主要的跨进程通信方式，binder 驱动和 service manager 分别相当于网络协议中的路由器和 DNS，并基于 mmap 实现了 IPC 传输数据时只需一次拷贝。
>
> binder 包括 BinderProxy、BpBinder 等各种 Binder 实体，以及对 binder 驱动操作的 ProcessState、IPCThreadState 封装，再加上 binder 驱动内部的结构体、命令处理，整体贯穿 Java、Native 层，涉及用户态、内核态，往上可以说到 Service、AIDL 等，往下可以说到 mmap、binder 驱动设备，是相当庞大、繁琐的一个机制。

**怎么理解页框和页**

> 页框是指一块实际的物理内存，页是指程序的一块内存数据单元。一个页框可以映射给多个页，也就是说一块实际的物理存储空间可以映射给多个进程的多个虚拟内存空间，这也是 mmap 机制依赖的基础规则。

**简单说下 binder 的整体架构吧**

> ​	Client 通过 ServiceManager 或 AMS 获取到的远程 binder 实体，一般会用 **Proxy** 做一层封装，比如 ServiceManagerProxy、 AIDL 生成的 Proxy 类。而被封装的远程 binder 实体是一个 **BinderProxy**。
>
> ​	**BpBinder** 和 BinderProxy 其实是一个东西：远程 binder 实体，只不过一个 Native 层、一个 Java 层，BpBinder 内部持有了一个 binder 句柄值 handle。
>
> ​	**ProcessState** 是进程单例，负责打开 Binder 驱动设备及 mmap；**IPCThreadState** 为线程单例，负责与 binder 驱动进行具体的命令通信。
>
> ​		由 Proxy 发起 transact() 调用，会将数据打包到 Parcel 中，层层向下调用到 BpBinder ，在 BpBinder 中调用 IPCThreadState 的 transact() 方法并传入 handle 句柄值，IPCThreadState 再去执行具体的 binder 命令。
>
> ​		由 binder 驱动到 Server 的大概流程就是：Server 通过 IPCThreadState 接收到 Client 的请求后，层层向上，最后回调到 **Stub** 的 onTransact() 方法。
>
> ​		当然这不代表所有的 IPC 流程，比如 Service Manager 作为一个 Server 时，便没有上层的封装，也没有借助 IPCThreadState，而是初始化后通过 binder_loop() 方法直接与 binder 驱动通信的。









#### View绘制









#### Recyclerview

四级缓存 https://www.jianshu.com/p/3e9aa4bdaefd

> RecyclerView有四级缓存，分别是Scrap、Cache、ViewCacheExtension和RecycledViewPool，缓存的对象是ViewHolder。Scrap和Cache分别是通过position去找ViewHolder可以直接复用；ViewCacheExtension自定义缓存，目前来说应用场景比较少却需慎用；RecycledViewPool通过type来获取ViewHolder，获取的ViewHolder是个全新，需要重新绑定数据。当你看到这里的时候，面试官再问RecyclerView的性能比ListView优化在哪里，我想你已经有答案。

RecyclerView优化 一句话思路 空间换取时间 使用内存空间来换取数据转化的时间

1.在Adapter中最好不要进行任何的逻辑操作，比如日期转换,字符串切割等等,可以在model内部自行添加一个参数使用
	by lazy 来存储数据转换后的结果,这样数据逻辑操作就只执行一次，而不会随着数据回收重复计算
2.新增删除数据不刷线全部，而是刷新局部
3.布局优化，尽量少的布局嵌套，尽量少的控件
4.对于一些RecyclerView嵌套RecyclerView的布局可以进行多布局展示，而不是使用嵌套
5.资源文件的读取,初始化的时候使用 by lazy 生成
6.如果RecyclerView条目高度固定，使用setHasFixedSize(true),避免多次测量条目高度
7.对于RecyclerView，如果不需要动画，就把条目显示动画取消setSupportsChangeAnimations(false)
8.在RecyclerView添加滑动监听，一些图片加载可以在RecyclerView快速滑动的时候不进行加载图片
9.对于一个页面中的多个RecyclerView,如果使用同一个Adapter，可以使用setRecycledViewPool(pool)，共用回收池，
	避免来每一个RecyclerView都创建一个回收池，特别是RecyclerView嵌套RecyclerView时候，
	内部的RecyclerView必定使用的都是同一个Adapter，这个时候就很有必要使用回收池了
10.视情况使用setItemViewCacheSize(size)来加大RecyclerView缓存数目，用空间换取时间提高流畅度
11.对于条目点击时间不要在复用部分进行setOnClickListener，这样会重复设置点击监听，而是应该创建一个listener对象，
	传入控件的id，和当前的条目position，通过id和position判断处理点击监听
12.可以进行预加载,重写LayoutManager的getExtraLayoutSpace()方法,可以返回屏幕高度,预先加载一屏幕高度的数据,
	视情况，例如：一个item就占据一个页面，RecyclerView滑动到第二张，此时第一张可见，RecyclerView无法找到可复用
	的View，此时会重新new一个出来，滑动卡顿，第三张及以后可以找到复用的View，滑动流畅

---





#### AMS



##### Android 系统启动流程

<img src="https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20220916100306710.png" alt="image-20220916100306710" style="zoom:30%;" />

- 开机加电后，CPU先执行**预设代码**、加载ROM中的引导程序**Bootloader**和Linux内核到RAM内存中去，然后初始化各种软硬件环境、加载驱动程序、挂载根文件系统，执行**init进程**。
- init进程会启动各种系统本地服务，如**SM**（ServiceManager）、MS（Media Server）、bootanim（开机动画）等，然后init进程会在解析init.rc文件后fork()出**Zygoto进程**。
- Zygote会启动Java虚拟机，通过jni进入Zygote的java代码中，并创建**socket**实现IPC进程通讯，然后启动**SystemServer**进程。
- **SystemServer**进程负责启动和管理整个framework，包括**AMS**（ActivityManagerService）、**WMS**（WindowManagerService）、PMS（PowerManagerService）等服务、同时启动binder线程池，当SystemServer进程将系统服务启动就绪以后，就会通知AMS启动Home。

```
#SystemServer
//如何启动AMS
//system_server 进程中 启动AMS，startService方法很简单，是通过传进来的class然后反射创建对应的service服务。所以此处创建的是 Lifecycle的实例， 然后通过startService启动了AMS服务
public static void main(String[] args) {
	new SystemServer().run();
}

private void run() {
	startBootstrapServices();  //启动了AMS
	startCoreServices();
	startOtherServices();
}

private void startBootstrapServices() {
	mActivityManagerService = mSystemServiceManager.startService(ActivityManagerService.Lifecycle.class).getService();
	mActivityManagerService.setSystemServiceManager(mSystemServiceManager);
	mActivityManagerService.setInstaller(installer);
	mActivityManagerService.initPowerManagement();
	mActivityManagerService.setSystemProcess();
}
```

- AMS通过Intent隐式启动的方式启动**Launcher**，Launcher根据已安装应用解析对应的xml、通过findBiewById()获得一个RecycleView、加载应用图标、最后成功展示App列表。

**Zygote 为什么不采用 Binder 机制进行 IPC 通信？**

> Binder 机制中存在 Binder 线程池，是多线程的，如果 Zygote 采用 Binder 的话就存在上面说的 fork() 与 多线程的问题了。其实严格来说，Binder 机制不一定要多线程，所谓的 Binder 线程只不过是 在循环读取 Binder 驱动的消息而已，只注册一个 Binder 线程也是可以工作的，比如 service manager 就是这样的。实际上 Zygote 尽管没有采取 Binder 机制，它也不是单线程的，但它在 fork() 前主动停止 了其他线程，fork() 后重新启动了。





##### launcher(app)启动流程

<img src="https://upload-images.jianshu.io/upload_images/9601136-3fd506a10d612d4e.png?imageMogr2/auto-orient/strip|imageView2/2/w/960/format/webp" alt="img" style="zoom:67%;" />

- 1.点击app图标，Launcher进程使用**Binder** IPC向systemserver进程发起startActivity请求；
- 2.systemserver进程收到1中的请求后，向zygote进程发送创建新进程的请求；(**Socket**)
- 3.zygote进程fork出新的App进程
- 4.App进程通过Binder IPC向systemserver进程发起attachApplication请求；(ActivityThread中main方法attach)
- 5、6.systemserver进程收到4中的请求后，通过Binder IPC向App进程发送scheduleLauncherActivity请求；
- 7.App进程的ApplicationThread线程收到5的请求后，通过handler向主线程发送LAUNCHER_ACTIVITY消息；
- 8.主线程收到7中发送来的Message后，反射创建目标Activity，回调oncreate等方法，开始执行生命周期方法

ActivityThread.main()主要干了三件事：

①一准备主线程Looper ,  并使其loop()轮询事件，而且还实例化了ActivityThread

```
android.app.ActivityThread#main
Looper.prepareMainLooper();
ActivityThread thread = new ActivityThread();
thread.attach(false, startSeq);
Looper.loop();
```

②并调用器**attach**方法，跨进程调用AMS中方法，经过一系列处理，又跨进程ApplicationThread的**bindApplication**发送handler给ActivityThread进行处理，反射创建Application实例，并调用application的**attach**（attachbase）和**oncreate**方法。

```
private void attach(boolean system, long startSeq) {//activityThead
			final IActivityManager mgr = ActivityManager.getService();
		    mgr.attachApplication(mAppThread, startSeq);
}

（applicatoin）thread.bindApplication(args...);  //AMS中

 private void handleBindApplication(AppBindData data) { //activityThread
	  app = data.info.makeApplication(data.restrictedBackupMode, null);//-->Application-attachBaseContext()  反射创建application
        if (!ArrayUtils.isEmpty(data.providers)) {
            installContentProviders(app, data.providers);                //--------->ContentProvider-onCreate()
        }
        mInstrumentation.callApplicationOnCreate(app);                   //--------->Application-onCreate()
}
```

③接着需要创建第一个Activity，也就是主Activity

```kotlin
mAtmInternal.attachApplication(app.getWindowProcessController());
//最后执行到
realStartActivityLocked
clientTransaction.addCallback(LaunchActivityItem.obtain(new Intent(r.intent)...)
LaunchActivityItem#execute
public void execute(ClientTransactionHandler client, IBinder token,PendingTransactionActions pendingActions) {
        client.handleLaunchActivity(r, pendingActions, null /* customIntent */);
}
```

后续就是act的启动了

<img src="https://raw.githubusercontent.com/AndroidJiang/HomePics/master/818/image-20220915222326532.png" alt="image-20220915222326532" style="zoom: 80%;" />

注意：

> App启动可以参考https://www.jianshu.com/p/3c62c5c5668d，
>
> Activity启动可以参考https://www.jianshu.com/p/1969ef6e545d
>
> 从系统启动一直到Activity渲染可以参考https://mp.weixin.qq.com/s/5mP_stp_nWQfBRo_keAPSg



**面试题**

**Application, Activity, ContentProvider启动顺序**

> Application->attachBaseContext =====>ContentProvider->onCreate =====>Application->onCreate =====>Activity->onCreate
>
> 从下面的分析可以看出，Provider的初始化实际上是在Application的初始化过程中发生的，而Activity是在这之后。
>
> <img src="https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20220928210822418.png" alt="image-20220928210822418" style="zoom:30%;" />

```
//ActivityThread
private void handleBindApplication(AppBindData data) {
        app = data.info.makeApplication(data.restrictedBackupMode, null);    //--------->Application-attachBaseContext()
        if (!ArrayUtils.isEmpty(data.providers)) {
            installContentProviders(app, data.providers);                //--------->ContentProvider-onCreate()
        }
        mInstrumentation.callApplicationOnCreate(app);                   //--------->Application-onCreate()
}

//LoadedApk
 public Application makeApplication(boolean forceDefaultAppClass,
        Instrumentation instrumentation) {
        app = mActivityThread.mInstrumentation.newApplication(   //-------->反射出application 并 attachBaseContext()
                cl, appClass, appContext); 
        appContext.setOuterContext(app);
     if (instrumentation != null) {
        instrumentation.callApplicationOnCreate(app);            //--------->instrumentation为空，不走onCreate()
        }
    }
    return app;
}
```

```
handleLaunchActivity-performLaunchActivity-callActivityOnCreate
```

**如何判断APP是否已经启动？**

>  AMS会保存一个ProcessRecord信息，有两部分构成，“uid + process”，每个应用工程序都有自己的uid，而process就是AndroidManifest.xml中Application的process属性，默认为package名。每次在新建新进程前的时候会先判断这个 ProcessRecord 是否已存在，如果已经存在就不会新建进程了，这就属于应用内打开 Activity 的过程了（不存在就走fork新进程）

**知识点要建立体系，上面部分就是扩展的点。**
比如:

1. 进程间通信IPC的Binder机制有哪些应用的场景？
2. IPC中Socket方式有哪些应用场景？
3. Application的Context上下文对象是在哪里初始化的？
4. ContentProvider是在哪里创建、初始化和发布的？
5. ContentProvider的onCreate()和Application的onCreate()方法哪个先执行？



##### startAtivity源码分析

https://www.jianshu.com/p/1969ef6e545d

<img src="https://raw.githubusercontent.com/AndroidJiang/HomePics/master/typora/image120.png" alt="img" style="zoom:60%;" /> 

> **realStartActivityLocked** 和**App启动中**后半段一样，然后通过一些操作，最终通过ClientTransaction.addCallback时添加的LaunchActivityItem中的execute()方法回调了ActivityThread中的handleLaunchActivity()方法，进而实例化了Activity(ActivityRecord)和Application



##### **setContent源码** 

**1 添加view**

<img src="https://raw.githubusercontent.com/AndroidJiang/HomePics/master/typora/image121.png" alt="img" style="zoom:80%;" /> 

总结：setContentView总的来说就是创建DecorView，DecorView是一个FrameLayout，然后根据style选择系统自带的布局文件，(例如有没有title，这里说一下这个布局文件根布局是linearLayout，如果有title则是有一个viewStub和两个FrameLayout：title的和content的，如果没有title则是一个viewstub和一个content的FrameLayout)，添加到DecorView上，最后再把我们自己的的activity_main这种layout添加到content这个FrameLyout上。

注意：Activity和AppCompatActivity有一些区别，但总体上的逻辑是不变的。

**2 渲染view（先addview添加到window，然后performTraversal绘制）**

<img src="https://raw.githubusercontent.com/AndroidJiang/HomePics/master/typora/image122.png" alt="img" style="zoom:40%;" /> 

```java
public void requestLayout() {
    if (!mHandlingLayoutInLayoutRequest) {
        checkThread();
        mLayoutRequested = true;
        scheduleTraversals();
    }
}
```

> ViewRootImpl.setView ----- view.assignparent(ViewRootImpl)   view的parent是viewrootimpl

<img src="https://raw.githubusercontent.com/AndroidJiang/HomePics/master/typora/image123.png" alt="img" style="zoom:70%;" /> 

 

**3 Measure （看表格）**

**注：若view没有重写onMeasure方法，那match 和wrap都走父布局的可用空间**<img src="https://raw.githubusercontent.com/AndroidJiang/HomePics/master/typora/image124.png" alt="img" style="zoom:80%;" />  

<img src="https://raw.githubusercontent.com/AndroidJiang/HomePics/master/typora/image125.png" alt="img" style="zoom:40%;" /> 

<img src="https://raw.githubusercontent.com/AndroidJiang/HomePics/master/typora/image126.png" alt="img" style="zoom:40%;" /> 

**终极大招：**

**Activity，DecorView，PhoneWindow和ViewRoot的作用和相关关系**

[**https://blog.51cto.com/u_14813744/2718692**]

https://mp.weixin.qq.com/s/6tEBj9b-Uuw7vT39infiAA

**LayoutInflater参数区分**

```kotlin
inflater.inflate(R.layout.linearlayout, ll, true);   // 根布局会被添加到父布局，根布局宽高属性生效；
inflater.inflate(R.layout.linearlayout, ll, false);   //根布局不会添加到父布局，根布局宽高属性生效；
inflater.inflate(R.layout.linearlayout, null, false);//根布局不会添加到父布局，根布局宽高属性失效
inflater.inflate(R.layout.linearlayout, null);    //根布局不会添加到父布局，根布局宽高属性失效
inflater.inflate(R.layout.linearlayout, ll);   // 根布局会被添加到父布局，根布局宽高属性生效；
```

**LayoutInflater 是通过 inflate 方法来解析 xml，解析 xml 分为以下几步**

> 1. 解析  layout 文件xml 找到根节点的名称
>
>    createViewFromTag()  ---- createView   反射
>
> 2. 通过映射 Class.forName() 创建根节点视图View，如果需要 attachToRoot，绑定到父视图， 就把根节点 View add 到父视图中
>
> 3. 循环遍历子节点，找到子节点名称
>
>    rInflateChildren() ---- rInflate---createViewFromTag（遍历）
>
> 4. 通过映射 Class.forName（）方法创建子视图，然后加入到根节点中

**findViewById原理**

```
//view
 case com.android.internal.R.styleable.View_id: 
                    mID = a.getResourceId(attr, NO_ID);//初始化时拿属性赋值id
                    break;
                    
 public final <T extends View> T findViewById(@IdRes int id) {
        return findViewTraversal(id);
    }
 protected <T extends View> T findViewTraversal(@IdRes int id) {
        if (id == mID) { //view中命中返回
            return (T) this;
        }
    }
    
 ViewGroup
  protected <T extends View> T findViewTraversal(@IdRes int id) {
        if (id == mID) {//viewGroup命中则返回
            return (T) this;
        }
        for (int i = 0; i < len; i++) {
            View v = where[i];
            if ((v.mPrivateFlags & PFLAG_IS_ROOT_NAMESPACE) == 0) {
                v = v.findViewById(id);//遍历子布局  
            }
        }
   }
```





---



#### WMS

**注意：WMS内容基于AMS中的setCotentView中**

**概念**

window：Toast  Dialog  Activity中都有window，显示

windowmanager：对window的管理

wms：window最终管理者，启动、添加、删除等

###### WMS启动

<img src="https://raw.githubusercontent.com/AndroidJiang/HomePics/master/818/image-20220924181252682.png" alt="image-20220924181252682" style="zoom:50%;" />

同AMS启动，startOtherServices

```
SystemServer
private void startOtherServices(@NonNull TimingsTraceAndSlog t) {
		wm = WindowManagerService.main(context, inputManager, !mFirstBoot, mOnlyCore,
        	new PhoneWindowManager(), mActivityManagerService.mActivityTaskManager);
}
WindowManagerService
public static WindowManagerService main() {
        return main(context, im, showBootMsgs, onlyCore, policy, atm,
                SurfaceControl.Transaction::new, Surface::new, SurfaceControl.Builder::new);
}
 public static WindowManagerService main() {
        DisplayThread.getHandler().runWithScissors(() ->
                sInstance = new WindowManagerService(context, im, showBootMsgs, onlyCore, policy,
                        atm, transactionFactory, surfaceFactory, surfaceControlFactory), 0);
        return sInstance;
    }
```



**window分类：**

> Application Window：应用程序窗口，Activity  （1-99）
>
> Sub Window：子窗口，不能独立存在，必须附着在其他窗口才可以，PopupWindow（Dialog好像不是）（1000-1999）
>
> System Window：系统窗口，输入法、音量条 （2000-2999）
>
> 窗口次序  层级越大，越靠前

**phonewindow何时创建**

> AMS中launch启动后，performLaunchActivity中反射创建activity，并且**attach**进行创建phonewindow

```kotlin
ActivityThread#performLaunchActivity
private Activity performLaunchActivity(ActivityClientRecord r, Intent customIntent) {
 			activity = mInstrumentation.newActivity(
                      cl, component.getClassName(), r.intent);    //反射得到activity
      activity.attach(appContext, this, getInstrumentation(), r.token,
                        r.ident, app, r.intent, r.activityInfo, title, r.parent,
                        r.embeddedID, r.lastNonConfigurationInstances, config,
                        r.referrer, r.voiceInteractor, window, r.configCallback,
                        r.assistToken);
}
activity#attach
final void attach(Context context, ...){
			mWindow = new PhoneWindow(this, window, activityConfigCallback); //创建phonewindow
}
```

**viewRootImpl什么时候创建**

handleresume-windowmanagerimpl.addview-》windowmangerglobal.addview中

```
WindowMangerGlobal#addView
 public void addView(View view, ...) {
		root = new ViewRootImpl(view.getContext(), display);
		mViews.add(view);//global中所有decorview集合
        mRoots.add(root);//global中所有viewRootImpl集合  
		root.setView(view, wparams, panelParentView, userId);
}
```

一个应用只会创建一个WindowMangerGlobal，ViewRootImpl和DecorView一一对应

###### **更新window**

```
WindowManagerGlobal#updateViewLayout
public void updateViewLayout(View view, ViewGroup.LayoutParams params) {
        final WindowManager.LayoutParams wparams = (WindowManager.LayoutParams)params;
        view.setLayoutParams(wparams);
        synchronized (mLock) {
            int index = findViewLocked(view, true);
            ViewRootImpl root = mRoots.get(index);
            mParams.remove(index);
            mParams.add(index, wparams);
            root.setLayoutParams(wparams, false);
        }
}
ViewRootImpl#setLayoutParams
void setLayoutParams(WindowManager.LayoutParams attrs, boolean newView) {
   		scheduleTraversals(); //刷新
}
```

<img src="https://raw.githubusercontent.com/AndroidJiang/HomePics/master/818/image-20220924122812641.png" alt="image-20220924122812641" style="zoom: 50%;" />





###### 事件分发

https://cloud.tencent.com/developer/article/1035833

https://blog.csdn.net/a469516684/article/details/84679033

![image-20231222173016454](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20231222173016454.png)

- 对于 dispatchTouchEvent，onTouchEvent，return true是终结事件传递。return false 是回溯到父View的onTouchEvent方法。
- ViewGroup 想把自己分发给自己的onTouchEvent，需要拦截器onInterceptTouchEvent方法return true 把事件拦截下来。
- ViewGroup 的拦截器onInterceptTouchEvent 默认是不拦截的，所以return super.onInterceptTouchEvent()=return false；
- View 没有拦截器，为了让View可以把事件分发给自己的onTouchEvent，View的dispatchTouchEvent默认实现（super）就是把事件分发给自己的onTouchEvent。

**ViewGroup**事件分发伪代码

```java
//阉割版 
public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean consume = false;
        if (onInterceptTouchEvent(ev)) {
            if (!(mOnTouchListener != null && mOnTouchListener.onTouchEvent(ev))) {
                consume = onTouchEvent(ev);
            }
        } else {
            consume = child.dispatchTouchEvent(ev);
        }
        return consume;
    }

    public void onTouchEvent(MotionEvent ev) {
        boolean consume = false;
        if (mOnClickListener != null) {
            mOnClickListener.onClick(this);
            consume = true;
        }
        return consume;
    }
//详细版
// mFirstTouchTarget 可以理解为存储可以处理Touch事件的子View（不包括自身）的数据结构
private TouchTarget mFirstTouchTarget;
public boolean dispatchTouchEvent(MotionEvent ev) {
    // 是否中断
    final boolean intercepted;
    // 仅在ACTION_DOWN 和 已确定处理的子View时 调用，一旦onInterceptTouchEvent返回true，
    //则后续将不会在被调用和接收事件。后面会讲返回true后，mFirstTouchTarget会被为null；
    if (action == MotionEvent.ACTION_DOWN
                    || mFirstTouchTarget != null) {
        intercepted = onInterceptTouchEvent(ev);
    }
    // 如果不被拦截，在ACTION_DOWN事件处理中遍历所有的子View，找寻可以处理Touch事件的目标子View
    // 然后封装到mFirstTouchTarget，如果子View的dispatchTouchEvent返回true，则认为是目标子View；
    if(!intercepted){
        if (action == MotionEvent.ACTION_DOWN) {
            if(child.dispatchTouchEvent(MotionEvent ev)){
                mFirstTouchTarget = addTouchTarget(child);
                break；
            }
        }
    }
    boolean handled;
    // 如果mFirstTouchTarget == null，调用自身onTouchEvent()
    if(mFirstTouchTarget == null){
        handled=onTouchEvent(ev);
    }else{
        // 应上面的逻辑，如果ACTION_MOVE传递过程中被拦截，则将mFirstTouchTarget置为null，并传递一个cancel事件，
        // 告诉目标子View当前动作被取消了，后续事件将不会再次被传递；
        if (intercepted){
            ev.action=MotionEvent.ACTION_CANCEL;
            handled=mFirstTouchTarget.child.dispatchTouchEvent(ev);
            mFirstTouchTarget=null;
        }else {
            // 调用目标子view的dispatchTouchEvent，这也是为什么，上面结论所述的，dispatchTouchEvent/onTouchEvent 
            // 在ACTION_DOWN事件返回true，不管子View返回什么值，都能收到后续事件，会出现所谓控制“失效”的现象。
            handled=mFirstTouchTarget.child.dispatchTouchEvent(ev);
        }
    }
    retrun handled;
}
```

**View**事件分发伪代码

```java
//阉割版 
public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean consume = false;
        consume = onTouchEvent(ev);
        return consume;
    }

    public void onTouchEvent(MotionEvent ev) {
        boolean consume = false;
        if (mOnClickListener != null) {
            mOnClickListener.onClick(this);
            consume = true;
        }
        return consume;
    }
//详细版
public boolean dispatchTouchEvent(MotionEvent event) {
    boolean result;
    if(mOnTouchListener !=null 
          && ENABLE 
          && mOnTouchListener.onTouch(this, event)){
        result = true;
    }
    if (!result && onTouchEvent(event)) {
        result = true;
    }
    
    return result;
}

public boolean onTouchEvent(MotionEvent event) {
    final boolean clickable = ((viewFlags & CLICKABLE) == CLICKABLE
                || (viewFlags & LONG_CLICKABLE) == LONG_CLICKABLE)
                || (viewFlags & CONTEXT_CLICKABLE) == CONTEXT_CLICKABLE;
    
    if(DISABLED){
        return clickable;
    }            
    if(clickable){
        switch (action) {
            case MotionEvent.ACTION_UP:
                onClick(this);//onLongClick(this)
                break;
        }
    }            
}

```



**ViewGroup如何执行自己的onTouchEvent**

> dispatchTouchEvent =  super/false ， onInterceptTouchEvent = true

**为什么DecorView不直接分发给ViewGroup?**

> 可能分发之前还需要特殊处理，而ViewGroup不具备这些，比如打电话过程中，脸碰到手机不响应时间等

 

###### 刷新流程

invalide

![image-20220924172005380](https://raw.githubusercontent.com/AndroidJiang/HomePics/master/818/image-20220924172005380.png)

![image-20220924171939726](https://raw.githubusercontent.com/AndroidJiang/HomePics/master/818/image-20220924171939726.png)

屏幕管理核心DisplayContent

![image-20220924213110033](https://raw.githubusercontent.com/AndroidJiang/HomePics/master/818/image-20220924213110033.png)





###### 渲染

![image-20231027154832451](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20231027154832451.png)



> VSync -> Choreographer调度绘制 -> SurfaceFlinger合成 -> 提交显示

![image-20231115151758460](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20231115151758460.png)

1. 主线程处于 Sleep 状态，等待 Vsync 信号
2. Vsync 信号到来，主线程被唤醒，Choreographer 回调 FrameDisplayEventReceiver.onVsync 开始一帧的绘制
3. 处理 App 这一帧的 Input 事件(如果有的话)
4. 处理 App 这一帧的 Animation 事件(如果有的话)
5. 处理 App 这一帧的 Traversal 事件(如果有的话)
6. 主线程与渲染线程同步渲染数据，同步结束后，主线程结束一帧的绘制，可以继续处理下一个 Message(如果有的话，IdleHandler 如果不为空，这时候也会触发处理)，或者进入 Sleep 状态等待下一个 Vsync
7. 渲染线程首先需要从 BufferQueue 里面取一个 Buffer(dequeueBuffer) , 进行数据处理之后，调用 OpenGL 相关的函数，真正地进行渲染操作，然后将这个渲染好的 Buffer 还给 BufferQueue (queueBuffer) , SurfaceFlinger 在 Vsync-SF 到了之后，将所有准备好的 Buffer 取出进行合成(这个流程在下面的SurfaceFlinger会讲解



**完整的流程：**

1. **调用 `scheduleTraversals()` 方法**

   - **操作**：`ViewRootImpl` 调用 `scheduleTraversals()` 方法来安排一次视图树的遍历和绘制。

   - **源码相关**：在 `ViewRootImpl` 中，`scheduleTraversals()` 方法会做以下事情：

     ```java
     void scheduleTraversals() {
         if (!mTraversalScheduled) {
             mTraversalScheduled = true;//保证同时间多次更改只会刷新一次，连续两次setText(),也只会走一次绘制流程
             mTraversalBarrier = mHandler.getLooper().getQueue().postSyncBarrier();
             mChoreographer.postCallback(
                     Choreographer.CALLBACK_TRAVERSAL, mTraversalRunnable, null);//发送异步消息，等待VSYNC到来
             notifyRendererOfFramePending();
             pokeDrawLockIfNeeded();
         }
     }
     //发送异步消息---scheduleFrameLocked---scheduleVsyncLocked
     #Choreographer
     private void scheduleVsyncLocked() {
        mDisplayEventReceiver.scheduleVsync();//jni层发送vsync请求
     }
     ```

     - **发布同步屏障**：`mHandler.getLooper().getQueue().postSyncBarrier()` 在消息队列中插入一个同步屏障，阻止普通消息的处理，确保异步绘制任务可以优先执行。
     - **发送异步回调**：`mChoreographer.postCallback(Choreographer.CALLBACK_TRAVERSAL, mTraversalRunnable, null)` 向 `Choreographer` 注册一个异步回调，安排 `mTraversalRunnable` 在下一帧处理视图遍历。

2. **接收 VSYNC 信号**

   - **操作**：`Choreographer` 接收到系统发出的 VSYNC 信号，这标志着屏幕即将进行刷新。

   - **源码相关**：`Choreographer` 在 `doFrame()` 方法中处理 VSYNC 信号，并调度注册的回调函数：

     ```java
     void doFrame(long frameTimeNanos) {
         // 处理 VSYNC 信号，调用注册的回调
         mFrameCallback.run();
     }
     ```

3. **执行视图遍历和绘制**

   - **操作**：`Choreographer` 调用 `ViewRootImpl.performTraversals()` 方法，开始对视图树的测量、布局和绘制。

   - **源码相关**：`ViewRootImpl` 中的 `performTraversals()` 方法：

     ```java
     void performTraversals() {
         // 测量阶段
         performMeasure();
         // 布局阶段
         performLayout();
         // 绘制阶段
         performDraw();
     }
     ```

4. **提交渲染内容**

   - **操作**：每个 `View` 的绘制内容通过 `Surface` 提交，确保绘制内容被缓存在 GPU 中。

   - **源码相关**：`View.draw()` 方法中，`Canvas` 的绘制操作完成后，会通过 `Surface` 类的 `lockCanvas()` 和 `unlockCanvasAndPost()` 方法提交内容：

     ```java
     void draw(Canvas canvas) {
         // 绘制内容到 Canvas
         canvas.drawText("Hello", 0, 0, paint);
         // 提交 Canvas 到 Surface
         mSurface.lockCanvas();
         mSurface.unlockCanvasAndPost(canvas);
     }
     ```

5. **`SurfaceFlinger` 收集并合成内容**

   - **操作**：`SurfaceFlinger` 负责从多个 `Surface` 收集绘制内容，并将这些内容合成成一个最终的图像。

   - **源码相关**：`SurfaceFlinger` 中的合成方法：

     ```cpp
     void SurfaceFlinger::composeSurfaces() {
         // 收集各个 Surface 的内容
         for (auto& layer : mLayers) {
             layer->draw(mCanvas);
         }
         // 合成到最终帧
         mDisplay->present();
     }
     ```

6. **最终画面显示在屏幕上**

   - **操作**：合成后的图像被传送到显示设备，更新屏幕内容。

   - **源码相关**：在 `SurfaceFlinger` 完成合成后，最终图像通过 `Display` 类的 `present()` 方法显示在屏幕上：

     ```cpp
     void Display::present() {
         // 更新显示内容
         mNativeDisplay->presentFrame();
     }
     ```

**总结**

1. **安排绘制**：`scheduleTraversals()` 方法发布同步屏障，注册异步绘制回调，通知渲染器并保持绘制锁。
2. **处理 VSYNC 信号**：`Choreographer` 接收到 VSYNC 信号，调度视图遍历和绘制任务。
3. **视图遍历和绘制**：`ViewRootImpl` 执行视图树的测量、布局和绘制。
4. **提交渲染内容**：每个 `View` 的绘制内容通过 `Surface` 提交到 GPU。
5. **合成内容**：`SurfaceFlinger` 收集并合成多个 `Surface` 的内容。
6. **显示画面**：最终合成的图像通过 `Display` 显示在屏幕上。



**小tips**

1. App没有更新操作，比如没有需要执行的动画、用户没有交互等，app层是不会渲染的。

   - 在静态场景下,硬件会继续发送垂直同步信号VSync，这是底层显示器的定时信号,与内容无关。
   - Choreographer线程会仍旧接收到VSync信号，但是会检查到没有需要重绘的View，所以它不会再调度绘制任务给SurfaceFlinger。
   - SurfaceFlinger收到空的绘制任务列表，它会直接将之前缓冲的帧再次提供给显示器显示，而不会进行实际的组合工作。

1. 硬件加速：
   原理：调用GPU代替CPU完成绘制的计算工作，从工作分摊和绘制机制优化来提升绘制速度。

   缺点：受到GPU绘制方式的限制，Canvas有些方法在硬件加速开启的时候会失效或者无法正常工作，所以在自定义控件的绘制操作中可以手动地去关闭硬件加速



###### 面试题

**onResume中直接测量宽高有效吗？那应该如何测量**

> 无效，因为decorview是在onResume后才去addview的
>
> 1.view.post(new Runnable) 添加消息到mActions，等待贵人调用executeActions去发送消息，view的渲染会调用（wm.addview-requestlayout-scheduleTraversals）
>
> ```java
> //VSYNC到来，发送异步消息，执行FrameHandler的run方法 doTraversal（三大步）
> public void onVsync(long timestampNanos, long physicalDisplayId, int frame) {
>    Message msg = Message.obtain(mHandler, this);
>    msg.setAsynchronous(true);
>    mHandler.sendMessageAtTime(msg, timestampNanos / TimeUtils.NANOS_PER_MS);
> }
> //
> void doTraversal() {
> if (mTraversalScheduled) {
>    mTraversalScheduled = false;
>    mHandler.getLooper().getQueue().removeSyncBarrier(mTraversalBarrier);//移除屏障
>    performTraversals();
> }
> }
> private void performTraversals() {
> 		host.dispatchAttachedToWindow(attachInfo, 0);//贵人来了
> 		mFitSystemWindowsInsets.set(mAttachInfo.mContentInsets);
> 		host.fitSystemWindows(mFitSystemWindowsInsets);
> 		performMeasure(childWidthMeasureSpec, childHeightMeasureSpec);//测量
> 		performLayout(lp, mWidth, mHeight); // 布局
> 		performDraw();//绘制
> }
> ```
>
> 然后调用view.dispatchAttachedToWindow（这就是贵人）
>
> ```java
> void dispatchAttachedToWindow(AttachInfo info, int visibility) {
> 		if (mRunQueue != null) {
> 				mRunQueue.executeActions(info.mHandler);//
> 				mRunQueue = null;
> 		}   
> 		onAttachedToWindow();
> }
> public void executeActions(Handler handler) {
> final HandlerAction[] actions = mActions;
> for (int i = 0, count = mCount; i < count; i++) {
>     final HandlerAction handlerAction = actions[i];
>     handler.postDelayed(handlerAction.action, handlerAction.delay);
> }
> }
> }
> ```
>
> **dispatchAttachedToWindow是在三大步之前调用的，那为什么可以获取宽高？**
>
> performTraversals的执行它本身就是在一个Runnable消息里，mRunQueue.executeActions()的消息必须得等performTraversals彻底执行完才能得到执行，并非一调用dispatchAttachedToWindow就会执行。
>
> 2.使用 **ViewTreeObserver**： 这是获取视图尺寸的最可靠方法。（小米wjy）
>
> ```
> myView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
>      @Override
>      public void onGlobalLayout() {
>          int observerWidth = myView.getWidth();
>          int observerHeight = myView.getHeight();
>          myView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
>      }
> });
> ```

**结论：**在 onCreate() /onResume()中使用 view.post() 通常可以获取到 View 的宽高，因为该 Runnable 会在视图绘制完成后执行。

然而，这并不是 100% 保证的，因为在某些特殊情况下（如复杂的布局或特定的设备），view.post() 可能会在视图完全测量之前执行。

对于绝对可靠的方法，建议使用 ViewTreeObserver 的 OnGlobalLayoutListener。



**onResume中Handler.post能拿到宽高吗**

> Handler.post在create无法拿到，在onResume也不可以，View.post都可以
>
> 在onCreate里把消息推到消息队列时，onResume()的消息都还没入队，绘制三大步也就没有执行，所以拿不到。
>
> 在onResume中，
>
> 1. `handleResumeActivity` 触发 `onResume`（老版本androidhandleResumeActivity还是msg触发，不影响最终结果）
>
> 2. `onResume` 中发送handler.post()消息
>
> 3. ```
>    wm.addView(decor, l); //添加decorview到wm，新建viewrootImpl.setView()-requestLayout-scheduleTravels
>    ```
>
> 4. 发送同步屏障（注意三大步的触发跟这个无关）
>
> 5. `Choreographer` 在下一个 VSYNC 到达时三大步(这是关键因素)
>
> 
>
> <img src="https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20231222141106774.png" alt="image-20231222141106774" style="zoom:50%;" />
>
> mTraversalRunnable中`performTraversals` 并不是由同步屏障消息直接触发的， **VSYNC 到来后被执行**
>
> <img src="https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20231124161203537.png" alt="image-20231124161203537" style="zoom:50%;" />
>
> 请注意：部分手机如魅族note9 android9系统，可能系统被定制了，会把FrameHandler这条消息插到最前面，而不是等待vsync到来才添加，导致会先执行测量方法，后面才会执行到onresume中的handler消息，这种场景下可以获取宽高！<img src="/Users/AJiang/Library/Application Support/typora-user-images/image-20240117183801272.png" alt="image-20240117183801272" style="zoom:50%;" />

**子线程可以更新ui吗？**

> 可以，执行快就行，没来得及到addview（decorview在onresume后add）中检查线程

**Activity，Window，View三者关**

> Activity是界面交互、业务逻辑、生命周期管理
>
> Window是显示的区域，view的载体
>
> View显示控件

**首次View绘制流程在什么时候**

> handlerOnResume中执行onResume后，WindowmangerImpl.addview  ----ViewRootImpl.performTravels----measure,layout,draw

**invalidate会马上进行屏幕刷新吗**

> 不会，申请，等下一个VSYNC同步信号才会刷新

最终调用到ViewRootImpl的invalidate------scheduleTraversals

**为什么主线程耗时会导致掉帧**

> 会影响下一帧绘制

**如果消息队列前面有几条同步消息，然后才是同步屏障，最后才是几条异步消息，vsync信号到来时，会如何处理这些消息呢**

> 先处理屏障前的同步消息，遇到屏障后，往后查找异步消息优先执行

![image-20240909181212575](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20240909181212575.png)

---



#### PMS

https://mp.weixin.qq.com/s/SCGBYGCYhe9M7vXYUySKrg

apk的安装可以总结为下面几步：



1. 不管apk是通过adb安装的（apk存储于PC的磁盘）还是应用市场安装的（apk存储于设备），首先apk会被拷贝到 /data/app/xxx.tmp目录下面（xxx是一个随机生成的字符串）
2. 在经过重重的验证、校验（签名、版本号），/data/app/xxx.tmp 目录会重命名为 /data/app/[randomStrA]/[packageName]-[randomStrB] 目录，也就是被拷贝的apk最终路径是 /data/app/[randomStrA]/[packageName]-[randomStrB]/base.apk 。同时会为apk生成一个唯一的id又称appid
3. 解析apk的AndroidManifest中的内容为ParsedPackage，ParsedPackage中的权限等信息经过验证通过后，ParsedPackage传递给PMS，这样其他使用者比如ActivityManagerService就可以从PMS获取刚安装apk的信息了。
4. 刚安装的apk的安装信息比如包名、版本、签名证书、安装时间等会存储到PackageSetting，PackageSetting会传递给Settings，Settings会把它持久化到packages.xml文件。
5. 创建app data根目录，app data根目录是apk运行期间数据存储的根目录，并且app data根目录只有当前apk程序有读写执行权，其他不用没有任何权限。
6. 对apk的dex进行优化，优化即使不成功也不影响apk的安装，dex优化可以保证app运行性能上的提升。
7. 发送安装成功广播。

------



#### **动画**

`补间动画`仅仅对画布操作，新位置并不响应点击事件，原位置响应。`属性动画`是通过修改view属性实现动画，新位置响应点击事件

##### **属性动画**

**Interpolator（插值器）**

决定的是变化趋势

AccelerateDecelerateInterpolator 默认差值器

**TypeEvaluator（估值器）**

根据插值器计算出当前属性值改变的百分比结合初始值和结束值来计算当前属性具体的数值。

> 如：动画进行了50%（初始值=100，结束值=200 ），那么匀速插值器计算出了当前属性值改变的百分比是50%，那么估值器则负责计算当前属性值 = 100 + （200-100）x50% = 150。

Demo：

1郭霖的PointA-PointB  https://blog.csdn.net/guolin_blog/article/details/43816093

2咱项目中的tab底部导航（没有用到，可以参考）

 

##### **MotionLayout**

连接布局过渡与复杂的手势处理。你可以把它想象成属性动画框架、过渡动画管理和CoordinatorLayout三种能力集于一身的框架。

 

##### 方案对比

目前较常见的动画实现方案有原生动画、帧动画、gif/webp、lottie/SVGA、cocos引擎，对于复杂动画特效的实现做个简单对比

| 方案        | 实现成本                             | 上手成本 | 还原程度           | 接入成本 |
| ----------- | ------------------------------------ | -------- | ------------------ | -------- |
| 原生动画    | 复杂动画实现成本高                   | 低       | 中                 | 低       |
| 帧动画      | 实现成本低，但资源消耗大             | 低       | 中                 | 低       |
| gif/webp    | 实现成本低，但资源消耗大             | 低       | 只支持8位颜色      | 低       |
| Lottie/SVGA | 实现成本低，部分复杂特效不支持       | 低       | 部分复杂特效不支持 | 低       |
| cocos2d引擎 | 实现成本高                           | 高       | 较高               | 较高     |
| AlphaPlayer | 开发无任何实现成本，一次接入永久使用 | 低       | 高                 | 低       |

**而在复杂动画特效高效实现的场景中，我们的备选方案会更少一些，可以将讨论集中在Cocos2d、Lottie、Webp和本文的AlphaPlayer上。**

**Lottie**

Lottie是非常优选的多平台动画效果解决方案，其简单原理是将AE动画导出的json文件解析成每个layer层级对象，再绘制成对应的Drawable，最后显示在View上。在不涉及mask和mattes等特性时性能非常优选，主要耗时基本集中在Canvas#draw()上而已，json解析时通过流读取的方式避免一次性加载全部json数据带来的OOM问题。

但是也存在部分不足：

1. 如果动画涉及到mask或mattes等特性时，需要生成2~3个临时bitmap实现动画效果，容易引起内存抖动，且涉及的canvas#saveLayer()和canvas#drawBitmap()会带来额外的耗时；
2. 如果动画中还直接使用了图片，在ImageAssetManager首次对图片解码是在主线程进行的（据了解在iOS的版本上，使用图片导致内存和CPU的性能消耗会更大）；
3. 主要耗时还是在draw()上，绘制区域越大耗时越长；
4. 目前支持的AE特性还有待完善，此外有一些特性在低版本上可能还会没有效果，设计资源时需要规避。（[Supported After Effect Features](http://airbnb.io/lottie/#/supported-features)）

实际使用过程中，最常见的首启全屏引导动画基本都会包含上面提到的前三点不足，这种情况下性能其实是大幅退化的。因此对于直播场景的复杂特效动画而言，Lottie就不是一个很合适的实现方案了。

**Cocos2d-x**

Cocos2d-x支持非常多的游戏功能，诸如精灵、动作、动画、粒子特效、骨骼动画等等，可以供开发者自由实现各种姿势的动画效果，再加上自身具备跨平台能力和轻量级，同时支持Lua作为开发语言，可以说是非常适合植入移动端作为动画效果实现方案的游戏引擎。

但实际使用维护中会面临很多问题：

1. 体积大，即使经过裁剪也还有2M左右的大小，如果不是核心场景需要基本很难允许接入；
2. 对开发者的技术栈有较高要求；
3. 无法满足快速迭代；
4. 维护难度大，尤其是在Android机型兼容的问题上。

**Webp**

Webp相比PNG和JPEG格式体积可以减少25%，在移动端的平台支持上也很全面，支持24位RGB色；缺点是资源体积比较大，而且使用的软解效率低下，低端机上有明显卡顿问题。

**AlphaPlayer**

相比于上面提到的几个方案，AlphaPlayer的接入体积极小（只有40KB左右），而且对动画资源的还原程度极高，资源制作时不用考虑特效是否支持的问题，对开发者和设计师都非常友好。通过接入ExoPlayer或者自研播放器可以解决系统播放器在部分机型上可能存在的兼容性问题，且能带来更好的解码性能。

---

#### Gradle

```
./gradlew clean assembleBigBlueDebug --console=plain         查看任务执行顺序（通点击gradle中任务类似）
./gradlew clean assembleBigBlueDebug --info									 查看详细执行log
```

```java
./gradlew task1 -PCHANNEL=123
//放在android{}闭包中可以直接访问内部变量
task task1 {
    doLast {
        println CHANNEL.toString() 
        println "${defaultConfig.versionName}"
    }
}
```







####  其他

#####  设备唯一id获取

imei在6.0申请权限，Android 9.0以后彻底禁止第三方应用获取设备的IMEI（即使申请了 READ_PHONE_STATE 权限）。所以，如果是新APP，不建议用IMEI作为设备标识；

 mac地址10.0后的地址也放弃了，不能读取mac地址

 解决方案：多个硬件设备属性id组合成一个id

> **AndroidId : 如：df176fbb152ddce,无需权限,极个别设备获取不到数据或得到错误数据，可以通过某些方式被改变或因为 bug 导致不可用，第三方 App 无保证可用性，项目中用了，如果获取不到，则randomUUID生成保存sp**
>
> IMEI : 如：23b12e30ec8a2f17，需要电话权限；项目中已去除
>
> **OAID:Android 10 之后的替代IMEI方案(aar库) 移动统一联盟**  https://www.jianshu.com/p/07bdad73fea1
>
> Mac: 如：6e:a5:....需要权限，高版本手机获得数据均为 02:00.....（不可使用）
>
> Build.BOARD  如：BLA  主板名称,无需权限,同型号设备相同Build.BRAND  如：HUAWEI  厂商名称,无需权限,同型号设备相同Build.HARDWARE  如：kirin970  硬件名称,无需权限,同型号设备相同







##### android12适配存储权限

**以下不需要权限**

> 内部存储沙盒  getData下  
>
> 如 getCacheDir ------ /data/data/com.xxx/cache   如glide默认缓存路径 
>
> 外部存储沙盒 getExternalFilesDir   
>
> 如 getExternalCacheDir() ----/storage/emulated/0/Android/data/com.learn.test/cache 咪咕小游戏
>
> device中目录其实就是sdcard/Android/data/com.learn.test/cache

**以下需要权限，卸载不会消失**

> 外部存储  getExternalStorageDirectory

权限框架引用三方库轮子哥的[XXPermissions](https://github.com/getActivity/XXPermissions)

问题：如果老sdk中有页面需要申请权限，但是不符合安全隐私（如下华为市场要求）<img src="https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20240110183825366.png" alt="image-20240110183825366" style="zoom:50%;" />

sdk中无法进行更改，需要在application中监听拦截，可以在权限弹窗前做提示弹窗处理，同意后再requestPermissions，同时提示顶部弹窗

```
ActivityCompat.setPermissionCompatDelegate(instance);
```

<img src="https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20240110184145744.png" alt="image-20240110184145744" style="zoom:50%;" />







##### **MultiDex**

https://blog.csdn.net/Androiddddd/article/details/108784748

5.0后的系统都内置了加载多个dex文件的功能，而在5.0之前，系统只可以加载一个主dex，其它的dex就需要采用一定的手段来加载。这也就是我们今天要讲的MultiDex。

```bash
defaultConfig {
    multiDexEnabled true
}
dependencies {
    compile 'com.android.support:multidex:1.0.1'
}
//要么继承MultiDexApplication 要么    MultiDex.install(this);
```

> **5.0以下这个dexElements 里面只有主dex（可以认为是一个bug），没有dex2、dex3...，MultiDex是怎么把dex2添加进去呢?** 答案就是反射`DexPathList`的`dexElements`字段，然后把我们的dex2添加进去，当然，dexElements里面放的是Element对象，我们只有dex2的路径，必须转换成Element格式才行，所以**反射DexPathList里面的makeDexElements 方法**，将dex文件转换成Element对象即可。

**原理：**

> 通过反射dexElements数组，将新增的dex添加到数组后面，这样就保证ClassLoader加载类的时候可以从新增的dex中加载到目标类，经过分析后最终MultipDex原理图如下：

<img src="https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20220928210558901.png" alt="image-20220928210558901" style="zoom:50%;" />

**那如何进行优化Multidex的install速度呢？**

注意：因为install只作用于5.0以下手机，所以也是针对低版本手机的优化

> 1.子线程install（不推荐），会报启动页缺失的class错误，还有contentprovider的缺失，因为这个启动比较早，如果没在主multidex就会报错，虽然可以指定哪些类在主dex，但是还是不靠谱，难维护
>
> ```java
> defaultConfig {
> //分包，指定某个类在main dex
> multiDexEnabled true
> multiDexKeepProguard file('multiDexKeep.pro') // 打包到main dex的这些类的混淆规制，没特殊需求就给个空文件
> multiDexKeepFile file('maindexlist.txt') // 指定哪些类要放到main dex
> }
> ```
>
> maindexlist.txt 文件指定哪些类要打包到主dex中，内容格式如下
>
> ```arduino
> com/lanshifu/launchtest/SplashActivity.class
> ```
>
> 2.今日头条方案
>
> 核心思想是将耗时的操作放在一个独立的进程中，从而避免阻塞主应用进程（但是也是等结束了才进行下一步，个人觉得没必要！）
>
> - 在主进程Application 的 attachBaseContext 方法中判断如果需要使用MultiDex，则创建一个临时文件，然后开一个进程（LoadDexActivity），显示Loading，异步执行MultiDex.install 逻辑，执行完就删除临时文件并finish自己。
> - 主进程Application 的 attachBaseContext 进入while代码块，定时轮循临时文件是否被删除，如果被删除，说明MultiDex已经执行完，则跳出循环，继续正常的应用启动流程。
> - 注意LoadDexActivity 必须要配置在main dex中。





##### 依赖方式

api、implementation和compile、compileOnly

api代替compile，依赖的库可以透传給上面，如A依赖B，那么B中api方式所依赖的库都可以給A使用。

implementation和上面相反，B中此方式依赖的库，A无法使用

compileOnly，和compile相似，只存在编译期，不会参与打包





##### 主题切换

<style name="Theme.NightModeDemo" parent="Theme.MaterialComponents.DayNight.DarkActionBar">
    <!-- Primary brand color. -->
    <item name="colorPrimary">@color/purple_500</item>
    <item name="colorPrimaryVariant">@color/purple_700</item>
    <item name="colorOnPrimary">@color/white</item>
    <!-- Secondary brand color. -->
    <item name="colorSecondary">@color/teal_200</item>
    <item name="colorSecondaryVariant">@color/teal_700</item>
    <item name="colorOnSecondary">@color/black</item>
    <!-- Status bar color. -->
    <item name="android:statusBarColor" tools:targetApi="l">?attr/colorPrimaryVariant</item>
    <!-- Customize your theme here. -->
</style>


```java
private fun switchMode() {
        when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_NO -> {  //当前为日间模式
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)  //切换为夜间模式
            }
            AppCompatDelegate.MODE_NIGHT_YES -> {  //当前为夜间模式
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)  //切换为日间间模式
            }
            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)  //切换为夜间模式
            }
        }
        recreate()  //需要调用该方法才能生效
    }
```

<img src="https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20230828152904814.png" alt="image-20230828152904814" style="zoom: 50%;" />





##### 语言切换



---





##### 反编译 

目前网上比较流行的反编译方式是 `apktool` + `dex2jar` + `jd-gui` 的方式。

```
brew install apktool 
brew install dex2jar
brew install jd-gui
```

- 新建个目录`反编译`，将apk拖进去
- cd进入到该目录下，执行`apktool d app-debug.apk `，主要看资源和配置
- `apktool b app-debug `生成dex，可能为多个
- `d2j-dex2jar app-debug/build/apk/classes.dex`，`dex2jar` 工具将 `dex` 文件转化成一个 `jar` 包
- jd-gui打开这个jar即可

[^https://blog.csdn.net/jiaweilovemingming/article/details/128121084]: 参考



##### AndroidP以上反射

https://zhuanlan.zhihu.com/p/59455212  元反射



------



##### Gradle

AGP7以上，module中依赖aar会提示报错https://www.jb51.net/article/281705.htm

**命名空间**

```
android {
    namespace 'com.yechaoa.gradlex'
    ...
}
```

AGP7以上`namespace` 取代了 androidmanifest中`package` 属性

**定义变量**

```
//如果放在根build，则作用全局，否则作用当前
buildscript {
    ext {
        kotlin_version = "1.5.0"
    }
}
// gradle.properties
kotlin_version = "1.5.0"
```

gradle.properties中定义的变量，在task中直接使用，其他地方得${project.core_ktx_version}使用

**ndk**

```java
ndk.dir=/Users/AJiang/Downloads/android_sdk/ndk-bundle
```

需要编译ndk的项目才需要在local.properties引入路径，目前已废弃，默认用sdk下的

**闭包**

类比kt中的高阶函数

```
 private fun setPrintln(lambda: (String) -> Unit) {
 		aaa
    lambda.invoke("ajiang")
    bbb
}
fun main() {
    setPrintln { param ->
        println(param)
    }
}
//groovy闭包中参数省略如下(其实调用了setPrintln函数中aaa，然后调用闭包函数，最后再bbb)
 setPrintln {
        
 }
```

**版本决议**

app和module之间依赖库版本不同，各种场景下如何决议查看https://juejin.cn/post/7215579793261117501

> - 一个模块当有多个相同依赖时，不管是哪里引入的，gradle总是优先选择最高版本；
> - 当有多个模块多个相同依赖且没有版本约束条件时，决议策略同上，选择最高版本；
> - force优先级高于strictly，如果二者同时显式声明，则会报错，推荐使用strictly；
> - 同时使用force强制依赖版本时，版本决议的结果跟依赖顺序有关，最早force的版本优先；

Gradle版本决议一般选择最高的版本，但有时候可能项目不兼容（如冬冬某个module下okhttp版本较高，三方设备不支持高版本，故降级），所以这时候我们就要去干预Gradle的版本决议来保证项目的编译运行，下面是比较有效的解决方案：

```
configurations.all {
    resolutionStrategy.eachDependency { DependencyResolveDetails details ->
        def requested = details.requested
        if (requested.group == 'com.squareup.okhttp3' && requested.name == 'okhttp') {
            details.useVersion '4.10.0'
        }
    }
}
```







------



##### debug小技巧

调试未启动进程的某个断点处可能会来不及，怎么处理？

> Debug.waitForDebugger()，这将导致进程B在这一点暂停执行，直到调试器连接。

**clipChildren 设置在父布局为什么无效**

<img src="https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20240626151427385.png" alt="image-20240626151427385" style="zoom:50%;" />

> 爷爷布局：FrameLayout    父布局：LinearLayout    子布局：Button
>
> LinearLayout里设置clipChildren=false时，因为爷爷布局(FrameLayout)没有设置该属性，因此还是会限定其子布局，也就是图上红色部分(父布局LinearLayout),此时，即使(父布局LinearLayout)没对子布局(Button)进行限制(clipChildrenfalse)，但是因为canvas已经在上个步骤被限制了，因此子布局(Button)展示的范围依然很小，所以要放在爷爷布局，这样父布局变大，子布局才不会收到影响

------

#####  Git

**本地**commit多个回退：

git **reset** --soft  或者用as工具右击对应commit选择reset Current Branch to here默认用soft，保留代码修改记录到暂存区，如果用hard，则会丢弃掉暂存区修改，如：

1-2-3(HEAD)    soft 下reset到1，那么HEAD到1上，2、3修改会保留在暂存区，如果用hard，则HEAD到1上，2、3修改丢西

**远程**多个回退：

**revert**用于挨个回退，如revert 3,revert 2，然后push

**reset**不建议用，比较暴力会影响其他人，git reset --hard 2，然后push

**rebase**









### 三方库源码

#### OkHttp

##### 网络

**osi模型**

应用层 ：应用进程之间的通信,例如HTTP、SMTP等协议。

传输层 ： 提供端到端的通信服务,TCP协议提供可靠传输,UDP提供非可靠传输。

网络层：决定数据包通过什么路径从源主机传输到目标主机,主要是IP协议。

数据链路层：负责在物理层的传输介质上传送数据帧，并在源主机和目的主机之间建立逻辑链路。

物理层：负责吧两台计算机连接起来，光纤、电缆等

> 1. **应用层**:浏览器作为应用程序向百度服务器的80端口发出**HTTP**请求。
> 2. **传输层**:**TCP**协议将请求报文分割成一个个TCP数据包,添加序号、校验码等头信息以确保可靠传输。
> 3. **网络层**:**IP**协议为数据包添加源IP地址和目标IP地址,用于在多个网络设备之间转发数据包。
> 4. **数据链路层**:网络接口卡给数据包添加**MAC地址、校验码**等用于在两台主机间可靠传输的头信息。
> 5. **物理层**:以比特流的形式封装前面各层的数据,通过**物理介质**发送比特,如RJ45的电信号。
> 6. 对端主机按照相反顺序进行处理,一层层打开封装,最终达到应用层交付给百度服务器。
> 7. 百度服务器返回响应,经过相反路径发送回来,最后在你的浏览器上显示请求的网页



**三次握手，四次挥手**

<img src="https://raw.githubusercontent.com/AndroidJiang/HomePics/master/818/image-20220830210251051.png" alt="image-20220830210251051" style="zoom: 50%;" />

<img src="https://raw.githubusercontent.com/AndroidJiang/HomePics/master/818/image-20220830210312479.png" alt="image-20220830210312479" style="zoom: 50%;" />

**为什么TCP连接的时候是3次？2次不可以吗？**

因为需要考虑连接时丢包的问题，如果只握手2次，第二次握手时如果服务端发给客户端的确认报文段丢失，此时服务端已经准备好了收发数(可以理解服务端已经连接成功)据，而客户端一直没收到服务端的确认报文，所以客户端就不知道服务端是否已经准备好了(可以理解为客户端未连接成功)，这种情况下客户端不会给服务端发数据，也会忽略服务端发过来的数据。

如果是三次握手，即便发生丢包也不会有问题，比如如果第三次握手客户端发的确认ack报文丢失，服务端在一段时间内没有收到确认ack报文的话就会重新进行第二次握手，也就是**服务端会重发SYN报文段**，客户端收到重发的报文段后会再次给服务端发送确认ack报文。

**为什么TCP连接的时候是3次，关闭的时候却是4次？**

因为只有在客户端和服务端都没有数据要发送的时候才能断开TCP。而客户端发出FIN报文时只能保证客户端没有数据发了，服务端还有没有数据发客户端是不知道的。而服务端收到客户端的FIN报文后只能先回复客户端一个确认报文来告诉客户端我服务端已经收到你的FIN报文了，但我服务端还有一些数据没发完，等这些数据发完了服务端才能给客户端发FIN报文(所以不能一次性将确认报文和FIN报文发给客户端，就是这里多出来了一次)。

**TCP和UDP区别**

TCP面向连接，UDP面向无连接，其他记不得



**HTTPS的SSL握手过程**

1 、首 先 ，客 户 端 A 访 问 服 务 器 B ，比 如 我 们 用 浏 览 器 打 开 一 个 网

页 www.baidu.com ，这时，浏览器就是客户端 A ，百度的服务器就是服务器 B 了。 这时候客户端 A 会生成一个随机数 1，把随机数 1 、自己支持的 SSL 版本号以及 加密算法等这些信息告诉服务器 B 。 

2、服务器 B 知道这些信息后，然后确认一下双方的加密算法，然后服务端也生成 一个随机数 B ，并将随机数 B 和 CA 颁发给自己的证书一同返回给客户端 A 。

3、客户端 A 得到 CA 证书后，会去校验该 CA 证书的有效性，校验方法在上面 已经说过了。校验通过后，客户端生成一个随机数 3 ，然后用证书中的公钥加密随 机数 3 并传输给服务端 B 。

4、服务端 B 得到加密后的随机数 3，然后利用私钥进行解密，得到真正的随机数 3。

5、最后，客户端 A 和服务端 B 都有随机数 1、随机数 2、随机数 3，然后双方利 用这三个随机数生成一个对话密钥。之后传输内容就是利用对话密钥来进行加解密 了。这时就是利用了对称加密，一般用的都是 AES 算法。

6、客户端 A 通知服务端 B ，指明后面的通讯用对话密钥来完成，同时通知服务 器 B 客户端 A 的握手过程结束。

7、服务端 B 通知客户端 A，指明后面的通讯用对话密钥来完成，同时通知客户端 A 服务器 B 的握手过程结束。

8、SSL 的握手部分结束，SSL 安全通道的数据通讯开始，客户端 A 和服务器 B 开 始使用相同的对话密钥进行数据通讯。



##### **请求流程** 

```
    OkHttpClient httpClient = new OkHttpClient();
    String url = "https://www.baidu.com/";
    Request getRequest = new Request.Builder().url(url).get().build();
    Call call = httpClient.newCall(getRequest);
    call.enqueue(new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
        }
        @Override
        public void onResponse(Call call, Response response) throws IOException {
        }
    });        
```

1.创建OkHttpClient、request实例，builder模式

2.异步请求：

```
void enqueue(AsyncCall call) {
    synchronized (this) {
      readyAsyncCalls.add(call); //存入等待执行的队列
    }
    promoteAndExecute(); //两处调用：添加异步请求时、同步/异步请求 结束时
  } 
    //调度的核心方法：在 控制异步并发 的策略基础上，使用线程池 执行异步请求
  private boolean promoteAndExecute() {
    List<AsyncCall> executableCalls = new ArrayList<>();
    boolean isRunning;
    synchronized (this) {
      for (Iterator<AsyncCall> i = readyAsyncCalls.iterator(); i.hasNext(); ) {
        AsyncCall asyncCall = i.next();
        if (runningAsyncCalls.size() >= maxRequests) break; //最大并发数64
        if (asyncCall.callsPerHost().get() >= maxRequestsPerHost) continue; //Host最大并发数5
        i.remove();//从等待队列中移除
        asyncCall.callsPerHost().incrementAndGet();//Host并发数+1
        executableCalls.add(asyncCall);//加入 可执行请求 的集合
        runningAsyncCalls.add(asyncCall);//加入 正在执行的异步请求队列
      }
      isRunning = runningCallsCount() > 0;//正在执行的异步/同步 请求数 >0
    }
    for (int i = 0, size = executableCalls.size(); i < size; i++) {
      AsyncCall asyncCall = executableCalls.get(i);
      asyncCall.executeOn(executorService());//线程池中执行请求 见下面
    }
    return isRunning;
  }
	protected void execute() {
      try {
        Response response = getResponseWithInterceptorChain();执行请求获取结果
        responseCallback.onResponse(RealCall.this, response);//回调结果
      } catch (IOException e) {
      } finally {
        client.dispatcher().finished(this);//请求结束 掉用promoteAndExecute继续遍历等待队列，执行可执行队列
      }
    }
```

**总结如下：**

1. 构建一个okhttpClient对象，传入你想传入的对象，不传就是默认的；

   ```
   val okHttpClient = OkHttpClient()
   //OR
   val okHttpClient = OkHttpClient.Builder().build();
   ```

2. 构建request对象

   ```
   Request request = new Request.Builder()  
   ```

3. 创建call对象，实际上返回的realCall类 ，继续调用RealCall.newRealCall

   ```
   var call = client.newCall(request);
   ```

4. 调用enqueue方法

   ```
   okHttpClient.newCall(request).enqueue(object : Callback {
       //异步
       override fun onFailure(call: Call, e: IOException) {
           e.printStackTrace()
       }
       @Throws(IOException::class)
       override fun onResponse(call: Call, response: Response) {
           Log.d("response:", response.body!!.string())
       }
   })
   ```

5. 如果当前这个call对象已经被运行的话，则抛出异常；继续调用dispatcher的enqueue方法，**如果当前运行队列<64并且正在运行，访问同一个服务器地址的请求<5，就直接添加到运行队列，并且开始运行**；不然就添加到等待队列；线程池创建（相当于缓存线程池），核心为0，不占用资源

   > 核心线程数 保持在线程池中的线程数量
   > 线程池最大可容纳的线程数  
   > 当线程池中的线程数量大于核心线程数，空闲线程就会等待60s才会被终止，如果小于就会立刻停止；
   > SynchronousQueue看下面面试题

6. 运行AsyncCall，调用它的execute方法

7. 在execute方法中处理完response之后，会在finally中调用dispathcer的finished方法；

8. 当前已经处理完毕的call从运行队列中移除掉；并且调用promoteCalls方法

9. promoteCalls方法中进行判断，如果运行队列数目大于等于64，如果等待队列里啥都没有，也直接return？
   循环等待队列，将等待队列中的数据进行移除，移除是根据运行队列中还能存放多少来决定；移到了运行队列中，并且开始运行；

##### **五大拦截器：责任链模式**

<img src="https://raw.githubusercontent.com/AndroidJiang/HomePics/master/typora/image198.png" alt="img" style="zoom:80%;" /> 

```
Response getResponseWithInterceptorChain() throws IOException {
  List<Interceptor> interceptors = new ArrayList<>();
  interceptors.addAll(client.interceptors());
  interceptors.add(retryAndFollowUpInterceptor);
  interceptors.add(new BridgeInterceptor(client.cookieJar()));
  interceptors.add(new CacheInterceptor(client.internalCache()));
  interceptors.add(new ConnectInterceptor(client));
  if (!forWebSocket) {
    interceptors.addAll(client.networkInterceptors());
  }
  interceptors.add(new CallServerInterceptor(forWebSocket));
  Interceptor.Chain chain = new RealInterceptorChain(
      interceptors, null, null, null, 0, originalRequest);
  return chain.proceed(originalRequest);
}
```

**RetryAndFollowInterceptor**（重试和重定向  2xx  3xx）

重试：router、io异常、协议、证书等问题-不重试

更多路线 [www.baidu.com](https://www.baidu.com) 对应两个ip，可以重试

重定向：取出location，封装request去请求<20次

**BridgeInterceptor**（桥接）

添加header 压缩 解压

**CacheInterceptor** 

只针对get，缓存

 **ConnectInterceptor**☆☆☆☆☆

内部维护连接池，负责连接复用。ConnectionPool存放socket连接（ip/port）,默认5个存活5分钟，连接服务器先从连接池中找，没有则新建。

```
StreamAllocation streamAllocation = realChain.streamAllocation();//重试拦截器中创建 直接取出
//findHealthyConnection-findConnection 连接池中查找，找不到则新建并addconnections（之前会clean remove掉无用connection）
HttpCodec httpCodec = streamAllocation.newStream(client, doExtensiveHealthChecks);
RealConnection connection = streamAllocation.connection();
```

**CallServerInterceptor**

连接服务器发起请求

参考：https://www.jianshu.com/p/32de43ce0252

##### **面试题：**

**如果想修改response的cache-control，那么自定义的拦截器用哪种？**

> 答：addNetworkInterceptor,在response回到cacheInterceptor处理之前吧header修改好，如果用addInterceptor，那没有任何效果，因为此处response已经经过CacheInterceptor了，无力回天。

**自定义log拦截器放在两个地方区别**

> 答：addNetworkInterceptor，处理完的request，真正的请求；
>
> addInterceptor，响应区别不大，响应更全。（正常用这个）

 **为什么建立连接协议是三次握手，而关闭连接却是四次握手呢？**

> 这是因为服务端的LISTEN状态下的SOCKET当收到SYN报文的连接请求后，它可以把ACK和SYN(ACK起应答作用，而SYN起同步作用)放在一个报文里来发送。但关闭连接时，当收到对方的FIN报文通知时，它仅仅表示对方没有数据发送给你了；但未必你所有的数据都全部发送给对方了，所以你可能未必会马上会关闭SOCKET,也即你可能还需要发送一些数据给对方之后，再发送FIN报文给对方来表示你同意现在可以关闭连接了，所以它这里的ACK报文和FIN报文多数情况下都是分开发送的。

 **http版本区别**

> http1.0:一次请求 会建立一个TCP连接，请求完成后主动断开连接。这种方法的好处是简单，各个请求互不干扰。
> 但每次请求都会经历 3次握手、2次或4次挥手的连接建立和断开过程——极大影响网络效率和系统开销。
>
> http1.1:**keep-alive机制**：一次HTTP请求结束后不会立即断开TCP连接，如果此时有新的HTTP请求，且其请求的Host同上次请求相同，那么会直接复用TCP连接。**连接的复用是串行的**
>
> http2.0:1.1中如果想并行请求，会建立多个tcp链接，增大网络开销。 **多路复用机制 就实现了 在同一个TCP连接上 多个请求 并行执行。**

**选择网络访问框架的时候，为什么要选择OkHttp而不是其他框架；**

> 明确一点：并不期待，你将市面上所有的框架都全部搞得非常清楚，优缺点全部列出来；你是否具备掌控网络访问框架的能力；
> 这个问题没有标准答案，最好是带点主观意识；
> OkHttp
> XUtil      支持网络请求，图片加载，甚至还能操作数据库；就我个人而言，我认为，一个好的网络访问框架应该只专注一件事
> Retrofit  肯定这个框架不错，它封装了OkHttp，所以我暂时没有去深入了解它，
> Volley  官方出品，官方介绍适合小中型app，接口比较多，访问量比较大；基于HttpUrlConnection封装，（HttpUrl。。。 android 2.3以下api）
> 就我个人而言，我更希望能够更加深入的去了解网络访问框架
>
> OkHttp基于Socket通信，它更倾向于底层，会对Http协议进行完全的封装，我在学习这个框架的时候，可以更加底层的了解；我相信只要我能搞定okhttp，那么其他的
> 访问框架，都很容易懂；

**OkHttp中为什么使用构建者模式？**

> 使用多个简单的对象一步一步构建成一个复杂的对象；
> 优点：当内部数据过于复杂的时候，可以非常方便的构建出我们想要的对象，并且不是所有的参数我们都需要进行传递；
> 缺点：代码会有冗余

**OkHttp线程池为什么使用SynchronousQueue？**

> ☆☆**SynchronousQueue**  OkHttp使用SynchronousQueue实现了请求任务的快速分配,确保每个请求都可以很快获取线程处理,避免排队延迟,提高响应速度。这就是OkHttp基于SynchronousQueue的请求流程概况。
>
> 1. 假设线程A、线程B、线程C同时调用OkHttp发起请求,每个线程都会创建一个RealCall,并把请求封装成Runnable提交给dispatcher。
> 2. dispatcher会使用无界的SynchronousQueue来接收这些请求Runnable。
> 3. 由于SynchronousQueue不存储元素,线程A、B、C在offer请求时都会被阻塞,等待线程池的消费。
> 4. 当线程池的某个线程调用take()时,会取出一个阻塞的线程对应的Runnable,比如A。
> 5. 线程A被唤醒并将连接请求的任务交给线程池处理。线程B和C仍然阻塞等待。
> 6. 线程池处理完线程A的请求后,会再次take(),这次取出的是线程B或C中的一个。
> 7. 这样线程池会重复take请求,SynchronousQueue实现公平调度,每个线程都会有机会被分配到线程池得到执行。
> 8. 由于不存储元素,SynchronousQueue可以快速提供可执行任务,不会出现排队阻塞。
>
> OkHttp线程中当设置最大线程数为时，如果无限制的创建，不仅会消耗系统资源，还会降低系统的稳定性。但是OkHttp肯定也考虑到这方面，所以OkHttp设置了**最大请求任务执行个数64个**，有了这个限制。这样即解决了这个问题同时也能获得最大并发量。
>
> **SynchronousQueue阻塞的请求存放在哪了？**
>
> SynchronousQueue 作为一个不存储元素的阻塞队列,当线程调用put/offer入队时,如果没有线程立即执行take/poll出队,则该入队线程会被阻塞,但是并不会将元素存储在队列中,而是由操作系统来维护这些被阻塞的线程。
>
> 具体来说,被阻塞的线程会被挂起,进入等待状态,存储在系统的等待队列(wait queue)中,而不是存放在 SynchronousQueue 队列里。等待队列由操作系统内核维护。
>
> 当有线程执行take/poll操作时,系统会从等待队列中选取一个线程作为匹配线程,使其入队操作成功, selected thread will be woken up and its element is handed directly to the dequeuing thread.
>
> 所以SynchronousQueue本身不存储任何元素,它完全依赖操作系统的等待队列(wait queue)来管理阻塞的线程。
>
> 线程被阻塞后进入等待状态,等待队列来存储和管理这些线程,直到线程被唤醒为止。
>
> 这也是SynchronousQueue可以做到零容量、不存储元素的关键原因。它将元素存储的功能完全交给操作系统等待队列来实现。

**怎么设计一个自己的网络访问框架，为什么这么设计？**

> 我目前还没有正式设计过网络访问框架，
> 如果是我自己设计的话，我会从以下两个方面考虑
> 1：先参考现有的框架，找一个比较合适的框架作为启动点，比如说，基于上面讲到的okhttp的优点，选择okhttp的源码进行阅读，并且将主线的流程抽取出，为什么这么做，因为okhttp里面虽然涉及到了很多的内容，但是我们用到的内容并不是特别多；保证先能运行起来一个基本的框架；
> 2：考虑拓展，有了基本框架之后，我会按照我目前在项目中遇到的一些需求或者网路方面的问题，看看能不能基于我这个框架进行优化，比如服务器它设置的缓存策略，
> 我应该如何去编写客户端的缓存策略去对应服务器的，还比如说，可能刚刚去建立基本的框架时，不会考虑HTTPS的问题，那么也会基于后来都要求https，进行拓展；
>
> 为什么要基于Okhttp，就是因为它是基于Socket，从我个人角度讲，如果能更底层的深入了解相关知识，这对我未来的技术有很大的帮助；

**为什么okhttp请求很快？有哪些优化**

> 基于socket，tcp多路复用，而httpurlconnection每次三次握手。最多有64个连接池connectpool，每次请求的connection从pool中取，keeplive，这样就可以复用，不用每次都握手



##### 拓展：**okhttp的长连接**

看咪咕项目，大概流程如下：

1.建立链接；

2.发送身份信息；

3.服务端拿到身份信息和会话进行绑定；

4.服务端指定身份，取出对应会话，进行发送信息。

客户端

```
  public void onOpen(WebSocket webSocket, Response response) {
        this.webSocket = webSocket;
        // 发送身份信息到服务器
        String userId = "123456"; // 替换为实际的用户身份信息
        webSocket.send(userId);
    }
```

服务端

```
public void onMessage(WebSocket webSocket, String text) {
        String[] parts = text.split(":");
        if (parts.length == 2) {
            String userId = parts[0];
            String message = parts[1];
            // 将用户身份信息与 WebSocket 会话关联
            userSessions.put(userId, webSocket);
            // 可以根据业务逻辑，向特定用户发送消息
            WebSocket userWebSocket = userSessions.get(userId);
            if (userWebSocket != null) {
                userWebSocket.send("Server: Hi, User " + userId + "! I received your message: " + message);
            }
        }
```

**什么是 OkHttp 长连接？**

> 答案：OkHttp 长连接是指 OkHttp 库在与服务器建立 HTTP 连接时，可以保持连接的状态，并在多次请求和响应之间重用同一个连接，从而减少了连接的建立和关闭的开销。这种方式可以提高网络性能，减少延迟和资源消耗。

**OkHttp 如何实现长连接？**

> 答案：OkHttp 实现长连接的方式是通过 HTTP/1.1 协议中的 "Connection: keep-alive" 头部字段来指示服务器保持连接的状态。OkHttp 在发送请求时，默认会添加 "Connection: keep-alive" 头部字段，从而告诉服务器保持连接。服务器在响应中也可以通过 "Connection: keep-alive" 头部字段来告诉 OkHttp 是否保持连接。

**OkHttp 如何处理长连接的超时？**

> 答案：OkHttp 默认使用连接池来管理长连接，并在连接池中维护了一个空闲连接的列表。当需要发送新的请求时，OkHttp 会首先尝试从连接池中获取一个可用的连接。如果连接池中没有可用的连接，或者连接池中的连接超过了设置的最大空闲连接数，OkHttp 会创建新的连接。OkHttp 还提供了连接超时和读写超时的设置，可以通过 OkHttpClient 的 connectTimeout() 和 readTimeout() 方法来设置连接超时和读写超时的时间。当连接超时或读写超时时，OkHttp 会自动关闭连接，并从连接池中移除该连接。

**OkHttp 长连接是否适用于所有的网络请求？**

> 答案：不适用。长连接适用于对同一服务器频繁进行多次请求和响应的场景，例如在 RESTful API 中进行多次请求和响应，或者在 WebSocket 通信中保持长连接。对于只进行一次请求和响应的场景，使用长连接反而可能增加了连接的维护和资源消耗。因此，在使用 OkHttp 时，需要根据具体的业务场景和需求来决定是否使用长连接。





------



#### Retrofit

构建者模式：4-5参数以上，并且可选

 

<img src="https://raw.githubusercontent.com/AndroidJiang/HomePics/master/typora/image199.png" alt="img" style="zoom:80%;" /> 

 

<img src="https://raw.githubusercontent.com/AndroidJiang/HomePics/master/typora/image200.png" alt="img" style="zoom:80%;" /> 

1.动态代理 (略)

2.异步请求回调默认在主线程

```java
public Retrofit build() {
        //这里仅贴出获取callbackExecutor的代码
        ...
        Executor callbackExecutor = this.callbackExecutor;
        if (callbackExecutor == null) {
           callbackExecutor = platform.defaultCallbackExecutor();
        }
        ...
     }
```

Retrofit中的build()中platform.defaultCallbackExecutor()时，将返回一个MainThreadExecutor对象，而，当执行MainThreadExecutor.execute的时候，通过new Handler(Looper.getMainLooper())讲子线程切到了UI线程。

3.如何默认在子线程呢

```kotlin
val client = Retrofit.Builder().apply {
                baseUrl("")
                callbackExecutor(ThreadExecutor())
                addConverterFactory(ScalarsConverterFactory.create())
                client(OkHttpHolder.okHttpClient)
            }.build()
    class ThreadExecutor : Executor {
        override fun execute(r: Runnable) {
            r.run()
        }
    }
```

4.CallAdapter是如何解析泛型的

```kotlin
interface YourApiService {
    @GET("your-endpoint")
    Call<List<User>> getUsers();
}
```

在这个例子中，Retrofit首先会将`List<User>`转换为一个`ParameterizedType`类型。然后，当您调用`getUsers()`方法时，Retrofit会使用反射来确定响应类型，并返回一个`Call`实例，其类型为`Call<List<User>>`。

------



#### Rxjava

<img src="https://raw.githubusercontent.com/AndroidJiang/HomePics/master/typora/image201.png" alt="img" style="zoom:80%;" /> 

<img src="https://raw.githubusercontent.com/AndroidJiang/HomePics/master/typora/image202.png" alt="img" style="zoom:80%;" /> 

 

 

subscribeOn(Sch.IO)

<img src="https://raw.githubusercontent.com/AndroidJiang/HomePics/master/typora/image203.png" alt="img" style="zoom:80%;" /> 

observieOn(Sch.MAIN)

<img src="https://raw.githubusercontent.com/AndroidJiang/HomePics/master/typora/image204.png" alt="img" style="zoom:80%;" /> 

 

 

补充：背压

<img src="https://raw.githubusercontent.com/AndroidJiang/HomePics/master/typora/image205.png" alt="img" style="zoom:50%;" /> 

  从发布者角度来解决发布-订阅速度不均衡的问题,主要是实现背压机制,根据订阅者的请求动态调节发布速率。主要措施包括:

1. 使用支持背压的发布者

比如RxJava 2中的Flowable,它通过背压请求可以动态控制发布速度。

1. 根据订阅者请求调整发布速率

当订阅者的请求量小于阈值时,降低发布频率;当请求量超过阈值时,增加发布频率。

1. 设置发布数据的缓冲区

当订阅者处理速度跟不上时,可以先缓存到缓冲区,等订阅者处理完了再发布。

1. 丢弃生产速度过快的数据

如果生产数据的本身速度无法降低,可以丢弃掉部分不那么重要的数据。

1. 将数据分批发布

不要全量立即发布出去,可以分批发布,间隔一段时间发布一次。

1. 使用线程控制发布速率

可以用单线程定时发布数据,而不是多线程高速发布,从而控制发布速率。

1. 增加发布者到订阅者的通信

让发布者可以及时感知订阅者的处理能力和进度,从而及时调整发布速率。

总之,发布者通过背压机制和速率控制很大程度上可以解决速度不均衡问题,与订阅者端改进相辅相成。

 

Kotlin中的Flow确实也提供了背压(backpressure)的支持。

Kotlin Flow 的背压实现原理如下:

1. 在集合转换操作中内置了缓冲区buffer,用来平衡发射器和消费者的处理能力。
2. 消费者可以通过flow的collect()方法的参数来控制请求的数据量。
3. Flow的缓冲区会根据消费者的请求动态调整,以控制发射器的发射速率。
4. 当缓冲区已满时,发射器会暂停发射,直到消费者消费了数据才继续发射。
5. 通过这种方式,防止了发射器发送数据过快导致OOM的问题。

所以Kotlin的Flow通过内置的缓冲区机制,可以很好地支持背压,帮助平衡发射器和消费者之间的处理速度差异。

相比RxJava全面拥抱背压理念,Kotlin Flow对背压的支持更加轻量和隐式一些。但其核心思路与RxJava中的背压概念是一致的。

---



#### **Glide**

> 注意glide3升级到4.8之前，需要做兼容，4.9之后也还是链式调用api，基本不变
>
> 1.创建某个继承于 AppGlideMoudle 的类
>
> 2.使用 @GlideModule 注解 （原理就是使用了annotationProcessor生成，也就是依赖com.github.bumptech.glide:compiler:4.8.0的作用）
>
> 3.Make Module app，或者暴力Rebuild，生成GlideApp

**(不看码牛，享学二期最新glide4.11主流程分析)**

```
RequestManager with = Glide.with(this);

RequestBuilder<Drawable> load = with.load(url);

load.into(iv);
```

![image-20220904140425347](https://raw.githubusercontent.com/AndroidJiang/HomePics/master/818/image-20220904140425347.png)

![image-20220904142605830](https://raw.githubusercontent.com/AndroidJiang/HomePics/master/818/image-20220904142605830.png)

![image-20220905214833510](https://raw.githubusercontent.com/AndroidJiang/HomePics/master/818/image-20220905214833510.png)

围绕时序图



 

磁盘缓存几种策略：

> DiskCacheStrategy.NONE： 表示不缓存任何内容。
>
> DiskCacheStrategy.RESOURCE： 在资源解码后将数据写入磁盘缓存，即经过缩放等转换后的图片资源。
>
> DiskCacheStrategy.DATA： 在资源解码前将原始数据写入磁盘缓存。
>
> DiskCacheStrategy.ALL ： 使用DATA和RESOURCE缓存远程数据，仅使用RESOURCE来缓存本地数据。
>
> DiskCacheStrategy.AUTOMATIC：它会尝试对本地和远程图片使用最佳的策略，平衡磁盘空间和获取图片成本。当你加载远程数据时，AUTOMATIC 策略仅会存储未被你的加载过程修改过的原始数据，因为这样相比缓存转换后的图占用磁盘空间更少。对于本地数据，AUTOMATIC 策略则会仅存储变换过的缩略图，因为即使你需要再次生成另一个尺寸或类型的图片，取回原始数据也很容易。默认使用这种缓存策略

 

Glide跟其他框架相比优势在哪里？

  1：生命周期得管理

  2：支持gif  picasso支持gif

  3：三级缓存，内存缓存中还分为活动缓存和内存缓存；活动缓存指得是讲正在使用得图片用弱引用缓存，使用完成后到内存缓存；再到磁盘缓存；

  4：占用内存小，它默认得编码格式是rgb565；  picasso用得argb8888 ImageLoader不支持gif图片加载 而且也很老了

 

如何设计自己的图片加载框架





 **Glide如果在子线程加载，会有啥问题？**

> 不会创建空白fragment，主线程才会去新建空白fragment监听生命周期，into会判断主线程
>
> ```
> public ViewTarget<ImageView, TranscodeType> into(@NonNull ImageView view) {
>  Util.assertMainThread();
> ```

**有内存缓存了为什么还要设计活动缓存**

> 如果只有内存缓存Lru，正在使用的图片很有可能被删除掉，会引起崩溃。

**三级缓存之间是如何进行互动的？**

> Lru和活动缓存里只有一份，一张图不会同时存在两个里面，只会移来移去，正在使用会放入活动，如果不用了（关闭act）则会移动到Lru中

**Glide设置rgb565真的有效吗？**

> 不一定，对于有透明通道的argb图片，仍然是argb8888的格式，而不是565，argb8888是32位（bit）,换算4字节，565图片是16位，换算2字节，内存占用少一半 。当设置图片格式为RGB_565的时候，并不是所有图片都会按照这个格式进行输出。在Glide内部，会读取原始图片的EXIF头信息，获取当前图片的格式。若当前格式的图片支持alpha通道，则还是会设置为ARGB_8888的格式。

**placeholder error fallback区别，以及设置？**

```java
onLoadFailed----setErrorPlaceholder
#SingleRequest
private synchronized void setErrorPlaceholder() {
        if(this.canNotifyStatusChanged()) {
            Drawable error = null;
            if(this.model == null) {
                error = this.getFallbackDrawable();//fallback Drawable 在请求的url/model为 null 时展示。如主动设置空头像
            }
            if(error == null) {
                error = this.getErrorDrawable();//未设置fallback取error
            }
            if(error == null) {
                error = this.getPlaceholderDrawable();//以上两个都未设置取palceholder
            }
            this.target.onLoadFailed(error);
        }
    }
```

 

 **如何保障一个fragment**

> 1 从集合中取   2 队列无需等待 马上工作（handler 不好理解）

**为什么使用glide，还不是其他几个库**

> fresco适用于海外低端机，放在匿名共享内存中，不用堆内存回收，更多内存加载应用程序

**如何切换其他框架？ImageLoader 如何扩展以及切换图片请求框架**

> 本框架默认使用 `Glide` 实现图片加载功能, 使用 **ImageLoader** 为业务层提供统一的图片请求接口, **ImageLoader** 使用策略模式和建造者模式, 可以动态切换图片请求框架 (比如说切换成 `Picasso`), 并且加载图片时传入的参数也可以随意扩展 ( **loadImage()** 方法在需要扩展参数时, 调用端也不需要改动, 全部通过 **Builder** 扩展, 比如您想让内部的图片加载框架, 清除缓存您只需要定义个 **boolean** 字段, 内部的图片加载框架根据这个字段 **if|else** 做不同的操作, 其他操作同理, 当需要切换图片请求框架或图片请求框架升级后变更了 **Api** 时, 这里可以将影响范围降到最低, 所以封装 **ImageLoader** 是为了屏蔽这个风险)

- 本框架默认提供了 **GlideImageLoaderStrategy** 和 **ImageConfigImpl** 简单实现了图片加载逻辑 (v2.5.0 版本后, 需要依赖 **arms-imageloader-glide** 扩展库, 并自行通过 GlobalConfigModule.Builder#imageLoaderStrategy(new GlideImageLoaderStrategy); 完成注册), 方便快速使用, 但开发中难免会遇到复杂的使用场景, 所以本框架推荐即使不切换图片请求框架继续使用 **Glide**, 也请按照下面的方法, 自行实现图片加载策略, 因为默认实现的 **GlideImageLoaderStrategy** 是直接打包进框架的, 如果是远程依赖, 当遇到满足不了需求的情况, 您将不能扩展里面的逻辑
- 使用 **ImageLoader** 必须传入一个实现了 **BaseImageLoaderStrategy** 接口的图片加载实现类从而实现动态切换, 所以首先要实现**BaseImageLoaderStrategy**, 实现时必须指定一个继承自 **ImageConfig** 的实现类, 使用建造者模式, 可以储存一些信息, 比如 **URL**、**ImageView**、**Placeholder** 等, 可以不断的扩展, 供图片加载框架使用

```
public class PicassoImageLoaderStrategy implements BaseImageLoaderStrategy<PicassoImageConfig> {
	 @Override
    public void loadImage(Context ctx, PicassoImageConfig config) {
                        Picasso.with(ctx)
    			.load(config.getUrl())
    			.into(config.getImageView());
    ｝
}
```

- 实现 **ImageConfig**, 使用建造者模式 (创建新的 **PicassoImageConfig** 适用于新项目, 如果想重构之前的项目, 使用其他图片加载框架, 为了避免影响之前的代码, 请继续使用默认提供的 **ImageConfigImpl** 或者您之前自行实现的 **ImageConfig**, 继续扩展里面的属性)

```
public class PicassoImageConfig extends ImageConfig {
    private PicassoImageConfig(Buidler builder) {
        this.url = builder.url;
        this.imageView = builder.imageView;
        this.placeholder = builder.placeholder;
        this.errorPic = builder.errorPic;
    }
    public static Buidler builder() {
        return new Buidler();
    }
    public static final class Buidler {
        private String url;
        private ImageView imageView;
        private int placeholder;
        protected int errorPic;
        private Buidler() {
        }
        public Buidler url(String url) {
            this.url = url;
            return this;
        }
        public Buidler placeholder(int placeholder) {
            this.placeholder = placeholder;
            return this;
        }
        public Buidler errorPic(int errorPic){
            this.errorPic = errorPic;
            return this;
        }
        public Buidler imagerView(ImageView imageView) {
            this.imageView = imageView;
            return this;
        }
        public PicassoImageConfig build() {
            if (url == null) throw new IllegalStateException("url is required");
            if (imageView == null) throw new IllegalStateException("imageview is required");
            return new PicassoImageConfig(this);
        }
    }
}
```

- 在 **App** 刚刚启动初始化时, 通过 [**GlobalConfigModule**](https://github.com/JessYanCoding/MVPArms/wiki#3.1) 传入上面扩展的 **PicassoImageLoaderStrategy**, 也可以在 **App** 运行期间通过 **AppComponent** 拿到 **ImageLoader** 对象后, 调用 **setLoadImgStrategy(new PicassoImageLoaderStrategy)** 替换之前的实现 (默认使用 `Glide`)

```
方法一: 通过GlobalConfigModule传入
public class GlobalConfiguration implements ConfigModule {
    @Override
    public void applyOptions(Context context, GlobalConfigModule.Builder builder) {
        builder.imageLoaderStrategy(new PicassoImageLoaderStrategy);
    }
}
方法二: 拿到 AppComponent 中的 ImageLoader, 通过方法传入
ArmsUtils.obtainAppComponentFromContext(context)
	.imageLoader()
	.setLoadImgStrategy(new PicassoImageLoaderStrategy());
使用方法:
ArmsUtils.obtainAppComponentFromContext(context)
	.imageLoader()
	.loadImage(mApplication, PicassoImageConfig
                .builder()
                .url(data.getAvatarUrl())
                .imagerView(mAvater)
                .build());
```

**Glide是如何复用的？**

> 

**Glide加载本地drawable后，换同名文件，重启加载的是哪张图？**

> 默认有缓存情况下，开发周期中是老图，如果上线新版本则是新图
>
> ```
> public RequestBuilder<TranscodeType> load(@RawRes @DrawableRes @Nullable Integer resourceId) {
>  return loadGeneric(resourceId).apply(signatureOf(AndroidResourceSignature.obtain(context)));
> }
> @NonNull
> public static Key obtain(@NonNull Context context) {
>  Key signature = ApplicationVersionSignature.obtain(context);
>  int nightMode =
>      context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
>  return new AndroidResourceSignature(nightMode, signature);
> }
> ```
>
> 会根据版本号识别，同时也兼容了夜间模式
>
> 如果是远程url，比如请求登录验证的人机图，url一样，但是每次图片不一样，可以添加signature(@NonNull Key signature)，或者直接禁用缓存DiskCacheStrategy.NONE



---

#### Arouter

参考：https://www.modb.pro/db/211855（很详细）

**原理分析：**

> 1.apt编译时**com.alibaba.android.arouter.routes.ARouter**开头的类
>
> 2.运行时-注入
>
> - 1 在Application的onCreate()里面我们调用了Arouter.init(this)。
> - 2 接着调用了ClassUtils.getFileNameByPackageNam()来获取所有"com.alibaba.android.arouter.routes"目录下的dex文件的路径。（开启线程池）
> - 3 然后遍历这些dex文件获取所有的calss文件的完整类名。
> - 4 然后遍历所有类名，获取指定前缀的类，然后通过反射调用它们的loadInto(map)方法，这是个注入的过程，都注入到参数Warehouse的成员变量里面了。
> - 5 其中就有Arouter在编译时生成的"com.alibaba.android.arouter.routes.ARouter$$Root.ARouter\$$Root$$app"类，它对应的代码:<"app", ARouter\$$Group$$app.class>就被添加到Warehouse.groupsIndex里面了
>
> 3.运行时-获取 
>
> ARouter.getInstance().build(path);最终是创建了个Postcard，保存了path和group

低端机上，尤其是对一些大的项目，它的`dex`文件多，再加上cpu性能差，整个耗时就更长了，对于初次启动的应用非常不友好

**如何提升arouter的启动速度**

> 字节码插装，arouter已经做好了aop的插件，引入对应插件即可，原理是ASM进行字节码插装transform

```text
private static void loadRouterMap() {
    registerByPlugin = false;
    //插桩后生成代码
    register("com.alibaba.android.arouter.routes.ARouter$$Root$$modulejava");
    register("com.alibaba.android.arouter.routes.ARouter$$Root$$modulekotlin");
}
register 反射对应类执行loadinto方法添加进集合
```



疑问1：为什么init时需要遍历整个apk?

> 因为arouter不知道哪些文件下是跟arouter路由表相关的，所以先找到对应的class文件然后反射loadinto

疑问2：为什么字节码插桩省时间了？

> 省去了遍历整个apk的过程，指向性的找到对应的模块名，然后反射loadinto



阉割版：咱项目中的用法

1.APT注解处理器在编译时期生成路由表RouterApp$modulename

```
public final class RouterApp$article_module extends RouterClass {
  @Override
  public void init() {
    map.put("article/ArticleDetailActivity",ArticleDetailActivity.class);
  }
}
```

2.在build中手动配置各个modulename[]

3.application中create进行init，将生成的路由表加载进内存map集合，方便后面读取

```
public void init(String[] modules) {
        for (String module : modules) {
            try {
                Class cls = Class.forName(packageName + ".RouterApp$" + module);
                RouterClass path = (RouterClass) cls.newInstance();
                path.init();
                routerClasses.add(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
```

3.跳转时根据key取出对应class文件进行跳转

```
ActivityUtil.getInstance().startActivity(context, "article/ArticleDetailActivity");
```

```
public void startActivity(Context context,String routerPath, Intent intent){
		Class cls = Router.getInstance().getClass(routerPath);
		if (!(context instanceof Activity)) {
  	intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
	}
	intent.setClass(context, cls);
	context.startActivity(intent);
}
```

总结：

> 三者apt生成代码都是一样的，没有优化空间，但是初始化查找过程有区别：
>
> - 原版：开线程池扫描遍历dex找出对应class，反射执行加载loadInto
> - 插件版：通过插件ASM字节码插桩方式，找出对应的类生成代码，反射执行loadinto，避免了扫描dex文件这种耗时的操作。
> - 阉割版：咱们应用阉割了Arouter，在build中手动填入各module名称，UI_MODULES生成在BuildConfig中，初始化时可以直接指向性反射对应的类名规则





------



#### EventBus

**创建**

双重检索单例创建EventBus实例

```
public static EventBus getDefault() {
    EventBus instance = defaultInstance;
    if (instance == null) {
        synchronized (EventBus.class) {
            instance = EventBus.defaultInstance;
            if (instance == null) {
                instance = EventBus.defaultInstance = new EventBus();
            }
        }
    }
    return instance;
}
```

**注册**

```
EventBus.getDefault().register(this);
public void register(Object subscriber) {
    Class<?> subscriberClass = subscriber.getClass();
    List<SubscriberMethod> subscriberMethods = subscriberMethodFinder.findSubscriberMethods(subscriberClass);
    synchronized (this) {
        for (SubscriberMethod subscriberMethod : subscriberMethods) {
            subscribe(subscriber, subscriberMethod);
        }
    }
}
List<SubscriberMethod> findSubscriberMethods(Class<?> subscriberClass) {
				if (ignoreGeneratedIndex) {
            subscriberMethods = findUsingReflection(subscriberClass); //反射
        } else {
            subscriberMethods = findUsingInfo(subscriberClass);//索引方式见面
}

private void subscribe(Object subscriber, SubscriberMethod subscriberMethod) {
    Class<?> eventType = subscriberMethod.eventType;
    Subscription newSubscription = new Subscription(subscriber, subscriberMethod);
    CopyOnWriteArrayList<Subscription> subscriptions = subscriptionsByEventType.get(eventType);
    if (subscriptions == null) {
        subscriptions = new CopyOnWriteArrayList<>();
        subscriptionsByEventType.put(eventType, subscriptions);
    } else {
        if (subscriptions.contains(newSubscription)) {
            throw new EventBusException("Subscriber " + subscriber.getClass() + " already registered to event " + eventType);
        }
    }
    int size = subscriptions.size();
    for (int i = 0; i <= size; i++) {
        if (i == size || subscriberMethod.priority > subscriptions.get(i).subscriberMethod.priority) {
            subscriptions.add(i, newSubscription);
            break;
        }
    }
    List<Class<?>> subscribedEvents = typesBySubscriber.get(subscriber);
    if (subscribedEvents == null) {
        subscribedEvents = new ArrayList<>();
        typesBySubscriber.put(subscriber, subscribedEvents);
    }
    subscribedEvents.add(eventType);
    //......粘性事件处理
}
```

首先获取订阅对象的`Class`,然后通过`SubscriberMethodFinder`类查找所有的订阅方法并封装在`SubscriberMethod`中,`SubscriberMethod`包含订阅方法的关键信息，比如线程模型,优先级,参数,是否为黏性事件等,`findSubscriberMethods()`方法返回的是一个`List`,说明一个订阅者可以订阅多种事件。

**发送**

```
while (!eventQueue.isEmpty()) {
    postSingleEvent(eventQueue.remove(0), postingState);//发送即移除
}

private void postSingleEvent(Object event, PostingThreadState postingState) throws Error {   
        for (int h = 0; h < countTypes; h++) {
             Class<?> clazz = eventTypes.get(h);
             subscriptionFound |= postSingleEventForEventType(event, postingState, clazz);
         }
    }
private void postToSubscription(Subscription subscription, Object event, boolean isMainThread) {
        switch (subscription.subscriberMethod.threadMode) {
            case POSTING:
                invokeSubscriber(subscription, event);//反射
                break;
            case MAIN:
                if (isMainThread) {
                    invokeSubscriber(subscription, event);//反射
                } else {
                    mainThreadPoster.enqueue(subscription, event);
                }
                break;
                。。。
        }
    }
```



**优化**

> 针对register部分进行apt优化，编译过程中生成类添加每个类里面的接受方法，所有的类添加进一个集合

疑问：索引？不走反射？

> 从遍历父类，父类如果找不到接受方法的信息，那么会走反射

```java
implementation "org.greenrobot:eventbus:3.1.1"
annotationProcessor 'org.greenrobot:eventbus-annotation-processor:3.1.1'


javaCompileOptions {
            annotationProcessorOptions {
                // 根据项目实际情况，指定辅助索引类的名称和包名
                arguments = [ eventBusIndex : 'com.eventbus.project.MyEventBusIndex' ]
            }
        }

```

```java
@Subscribe(threadMode = ThreadMode.MAIN)
	public void handleSubscribeEvent(TimerTask event) {
}
@Subscribe(threadMode = ThreadMode.MAIN)
	public void handleSubscribeEvent(Test.Sub.A event) {
}
```

```java
EventBus.builder().addIndex(new MyEventBusIndex()).installDefaultEventBus();
```

```java
public class MyEventBusIndex implements SubscriberInfoIndex {
    private static final Map<Class<?>, SubscriberInfo> SUBSCRIBER_INDEX;
    static {
        SUBSCRIBER_INDEX = new HashMap<Class<?>, SubscriberInfo>();
        putIndex(new SimpleSubscriberInfo(me.jessyan.mvparms.demo.mvp.ui.activity.LoginActivity.class, true,
                new SubscriberMethodInfo[] {
            new SubscriberMethodInfo("handleSubscribeEvent", java.util.TimerTask.class, ThreadMode.MAIN),
            new SubscriberMethodInfo("handleSubscribeEvent", simple05.s04.Test.Sub.A.class, ThreadMode.MAIN),
        }));
    }
    private static void putIndex(SubscriberInfo info) {
        SUBSCRIBER_INDEX.put(info.getSubscriberClass(), info);
    }
    @Override
    public SubscriberInfo getSubscriberInfo(Class<?> subscriberClass) {//注册是进行查找
        SubscriberInfo info = SUBSCRIBER_INDEX.get(subscriberClass);
        if (info != null) {
            return info;
        } else {
            return null;
        }
    }
}
```



![image-20230915153359354](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20230915153359354.png)

索引源码解析

register

```java
private List<SubscriberMethod> findUsingInfo(Class<?> subscriberClass) {
    FindState findState = prepareFindState();
    findState.initForSubscriber(subscriberClass);
    while (findState.clazz != null) {
        findState.subscriberInfo = getSubscriberInfo(findState);  //从MyEventBusIndex中查找接受方法
        if (findState.subscriberInfo != null) {//索引类找到有接受方法
            SubscriberMethod[] array = findState.subscriberInfo.getSubscriberMethods();
            for (SubscriberMethod subscriberMethod : array) {
                if (findState.checkAdd(subscriberMethod.method, subscriberMethod.eventType)) {
                    findState.subscriberMethods.add(subscriberMethod);
                }
            }
        } else {
            findUsingReflectionInSingleClass(findState);//索引类找不到，则继续走反射一次
        }
        findState.moveToSuperclass();//上升到父类，比如A继承B，那么A中注册也会遍历父类看有没有接受方法
    }
    return getMethodsAndRelease(findState);
}
```

post 同上

> 注意：有个小坑绕进去了，子类A继承BaseAct，都有接受同样的event，子类会重载啊，他们两其实就是一个A类，别想岔劈了，base



**粘性**

先发送，后注册

```java
if (subscriberMethod.sticky) {
    if (eventInheritance) {
        Set<Map.Entry<Class<?>, Object>> entries = stickyEvents.entrySet();
        for (Map.Entry<Class<?>, Object> entry : entries) {
            Class<?> candidateEventType = entry.getKey();
            if (eventType.isAssignableFrom(candidateEventType)) {
                Object stickyEvent = entry.getValue();
                checkPostStickyEventToSubscription(newSubscription, stickyEvent);
            }
        }
    } else {
        Object stickyEvent = stickyEvents.get(eventType);
        checkPostStickyEventToSubscription(newSubscription, stickyEvent);
    }
}
-------->postToSubscription
```























---



 

#### Dagger2

<img src="https://raw.githubusercontent.com/AndroidJiang/HomePics/master/typora/image207.png" alt="img" style="zoom:80%;" /> 





#### LeakCanary

原理https://juejin.cn/post/7028491809185595399

老版本需要手动初始化 LeakCanary.install(this);新版本只需集成包，contentProvider即可自动完成初始化（延伸小米启动面试题）

```kotlin
internal sealed class AppWatcherInstaller : ContentProvider() {
  internal class MainProcess : AppWatcherInstaller()
  override fun onCreate(): Boolean {
    val application = context!!.applicationContext as Application
    // ContentProvider会在Application创建后就执行创建并执行onCreate()
    // 在onCreate()里调用单例AppWatcher的manualInstall方法
    AppWatcher.manualInstall(application)
    return true
  }
}
```

##### 自动检测Activity等

```java
#ActivityDestroyWatcher  lifecycleCallbacks
override fun onActivityDestroyed(activity: Activity) {
    objectWatcher.watch(activity, "")
  }
```

objectWatcher判断观察的对象是否被回收的原理是：**在创建WeakReference对象时可以指定一个ReferenceQueue对象，当该WeakReference指向的对象被GC标记可以回收后，该WeakReference会被加入到ReferenceQueue的末尾。**

```kotlin
public class WeakReference<T> extends Reference<T> {
    /**
     * 创建一个指向传入对象的弱引用，队列为null，普通弱引用方式
     */
    public WeakReference(T referent) {
        super(referent);
    }
    /**
     * 创建一个指向传入对象的弱引用，并且将该弱引用注册到指定ReferenceQueue
     * 当弱引用所指向的对象被GC标记可以回收后，该弱引用会被放到ReferenceQueue的末尾
     */
    public WeakReference(T referent, ReferenceQueue<? super T> q) {
        super(referent, q);
    }
}
```

伪代码

```kotlin
public abstract class Reference<T> {
   private T referent; 
   private ReferenceQueue<? super T> queue;
   public void clear() {
     this.referent = null;
     this.queue.enqueue(this);
   }
}
```

<img src="https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20231023155418388.png" alt="image-20231023155418388" style="zoom: 50%;" />

```kotlin
#ObjectWatcher
private val queue = ReferenceQueue<Any>()  //所有弱引用对象关联到的ReferenceQueue，判断弱引用所指向的对象是否被回收
fun watch(watchedObject: Any,description: String) {
  removeWeaklyReachableObjects()
  val key = UUID.randomUUID().toString()
  val watchUptimeMillis = clock.uptimeMillis()
  val reference =KeyedWeakReference(watchedObject, key, description, watchUptimeMillis, queue)
  watchedObjects[key] = reference
  checkRetainedExecutor.execute { //5s后执行
    moveToRetained(key)
  }
```

```kotlin
  /**
  * 在当前观察的对象列表中，将已经被回收的对象移除，剩余的其他对象就是可能发生泄漏的对象
  */
  @Synchronized private fun moveToRetained(key: String) {
    // 移除掉引用队列中不为空对应的对象（已经被回收的对象）
    removeWeaklyReachableObjects()
    val retainedRef = watchedObjects[key]
    if (retainedRef != null) {
      retainedRef.retainedUptimeMillis = clock.uptimeMillis()
      // 剩余可能发生泄漏的对象，则会调Listener的方法做进一步分析(4.2小节分析源码)
      onObjectRetainedListeners.forEach { it.onObjectRetained() }
    }
  }
```

```kotlin
 private fun removeWeaklyReachableObjects() {
    var ref: KeyedWeakReference?
    do {
      // ReferenceQueue中有该WeakReference，则说明该WeakReference指向的对象已经被回收,则从观察列表移除
      ref = queue.poll() as KeyedWeakReference?
      if (ref != null) {
        watchedObjects.remove(ref.key)
      }
    } while (ref != null)
  }
```

**总结：**

- leakcanary2.x利用ContentProvider在Application创建后就创建的原理，在ContentProvider创建时即完成leakcanary初始化，方便开发者使用。
- leakcanary通过在对象销毁时将引用传递给objectWatcher，由objectWatcher判断对象是否可能存在内存泄漏。
- objectWatcher在Activity销毁5s后，在主线程判断对象是否只被GC标记回收，如果没有则认为**可能**存在内存泄漏，接着触发HeapDumpTrigger（在4.2小节分析）在工作线程再次检查是否存在内存泄漏，是则导出hpfo文件**shark**并分析内存泄漏的引用链。





### 黑科技

#### 热修复

##### **Robust** 

即时生效，简单代码帮助理解：

![image-20230722194413749](https://raw.githubusercontent.com/AndroidJiang/HomePics/master/818/image-20230722194413749.png)

https://juejin.cn/post/6844903993898958856#heading-6

1、**基础包插桩**

打基础包时插桩，在每个方法前插入一段类型为 ChangeQuickRedirect 静态变量的逻辑，插入过程对业务开发是完全透明

```
public static ChangeQuickRedirect u;
protected void onCreate(Bundle bundle) {
        if (u != null) {
            if (PatchProxy.isSupport(new Object[]{bundle}, this, u, false, 78)) {
                PatchProxy.accessDispatchVoid(new Object[]{bundle}, this, u, false, 78);
                return;
            }
        }
        super.onCreate(bundle);
        ...
    }
```

发生在Class字节码生成后(打包成dex之前，编译时之后)，也称为Class字节码手术刀(robust可以选择ASM或者javassist)

robust是如何在每个方法里加代码，自定义插件transform，class->dex期间

2、**生成补丁包**

3、**加载补丁包**

从补丁包中读取要替换的类及具体替换的方法实现，新建ClassLoader加载补丁dex。当changeQuickRedirect不为null时，可能会执行到accessDispatch从而替换掉之前老的逻辑，达到fix的目的

修复代码如下：

```
public class PatchExecutor extends Thread {
    @Override
    public void run() {
        ...
        applyPatchList(patches);
        ...
    }
    /**
     * 应用补丁列表
     */
    protected void applyPatchList(List<Patch> patches) {
        ...
        for (Patch p : patches) {
            ...
            currentPatchResult = patch(context, p);
            ...
            }
    }
     /**
     * 核心修复源码
     */
    protected boolean patch(Context context, Patch patch) {
        ...
        //新建ClassLoader
        DexClassLoader classLoader = new DexClassLoader(patch.getTempPath(), context.getCacheDir().getAbsolutePath(),
                null, PatchExecutor.class.getClassLoader());
        patch.delete(patch.getTempPath());
        ...
        try {
            patchsInfoClass = classLoader.loadClass(patch.getPatchesInfoImplClassFullName());
            patchesInfo = (PatchesInfo) patchsInfoClass.newInstance();
            } catch (Throwable t) {
             ...
        }
        ...
        //通过遍历其中的类信息进而反射修改其中 ChangeQuickRedirect 对象的值
        for (PatchedClassInfo patchedClassInfo : patchedClasses) {
            ...
            try {
                oldClass = classLoader.loadClass(patchedClassName.trim());
                Field[] fields = oldClass.getDeclaredFields();
                for (Field field : fields) {
                    if (TextUtils.equals(field.getType().getCanonicalName(), ChangeQuickRedirect.class.getCanonicalName()) && TextUtils.equals(field.getDeclaringClass().getCanonicalName(), oldClass.getCanonicalName())) {
                        changeQuickRedirectField = field;
                        break;
                    }
                }
                ...
                try {
                    patchClass = classLoader.loadClass(patchClassName);
                    Object patchObject = patchClass.newInstance();
                    changeQuickRedirectField.setAccessible(true);
                    changeQuickRedirectField.set(null, patchObject);
                    } catch (Throwable t) {
                    ...
                }
            } catch (Throwable t) {
                 ...
            }
        }
        return true;
    }
}
```

**优点**

- 高兼容性（Robust只是在正常的使用DexClassLoader）、高稳定性，修复成功率高达99.9%
- 补丁实时生效，不需要重新启动
- 支持方法级别的修复，包括静态方法
- 支持增加方法和类
- 支持ProGuard的混淆、内联、优化等操作

**缺点**

- 代码是侵入式的，会在原有的类中加入相关代码
- so和资源的替换暂时不支持
- 会增大apk的体积，平均一个函数会比原来增加17.47个字节，10万个函数会增加1.67M













##### **QQ空间**

核心classloader

<img src="https://raw.githubusercontent.com/AndroidJiang/HomePics/master/typora/image64.png" alt="img" style="zoom:80%;" /> 

<img src="https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/webp-20220929180349081" alt="img" style="zoom:50%;" />

不足：

1.不支持即时生效，必须通过重启才能生效。

2.为了实现修复这个过程，必须在应用中加入两个dex!dalvikhack.dex中只有一个类，对性能影响不大，但是对于patch.dex来说，修复的类到了一定数量，就需要花不少的时间加载。对手淘这种航母级应用来说，启动耗时增加2s以上是不能够接受的事。

3.在ART模式下，如果类修改了结构，就会出现内存错乱的问题。为了解决这个问题，就必须把所有相关的调用类、父类子类等等全部加载到patch.dex中，导致补丁包异常的大，进一步增加应用启动加载的时候，耗时更加严重。



##### 微信Tinker

高版本自定义classloader反射替换pathclassloader？

微信针对QQ空间超级补丁技术的不足提出了一个提供DEX差量包，整体替换DEX的方案。主要的原理是与QQ空间超级补丁技术基本相同，区别在于不再将patch.dex增加到elements数组中，而是差量的方式给出patch.dex，然后将patch.dex与应用的classes.dex合并，然后整体替换掉旧的DEX文件，以达到修复的目的

<img src="https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20220929180239831.png" alt="image-20220929180239831" style="zoom: 67%;" />

<img src="https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/webp" alt="img" style="zoom:50%;" />

不足：

1.与超级补丁技术一样，不支持即时生效，必须通过重启应用的方式才能生效。

2.需要给应用开启新的进程才能进行合并，并且很容易因为内存消耗等原因合并失败。

3.合并时占用额外磁盘空间，对于多DEX的应用来说，如果修改了多个DEX文件，就需要下发多个patch.dex与对应的classes.dex进行合并操作时这种情况会更严重，因此合并过程的失败率也会更高。





**几种热修复框架选型**

| 方案对比   | Sophix               | Tinker                       | nuwa | AndFix | Robust | Amigo |
| :--------- | :------------------- | :--------------------------- | :--- | :----- | :----- | :---- |
| 类替换     | yes                  | yes                          | yes  | no     | no     | yes   |
| So替换     | yes                  | yes                          | no   | no     | no     | yes   |
| 资源替换   | yes                  | yes                          | yes  | no     | no     | yes   |
| 全平台支持 | yes                  | yes                          | yes  | no     | yes    | yes   |
| 即时生效   | 同时支持             | no                           | no   | yes    | yes    | no    |
| 性能损耗   | 较少                 | 较小                         | 较大 | 较小   | 较小   | 较小  |
| 补丁包大小 | 小                   | 较小                         | 较大 | 一般   | 一般   | 较大  |
| 开发透明   | yes                  | yes                          | yes  | no     | no     | yes   |
| 复杂度     | 傻瓜式接入           | 复杂                         | 较低 | 复杂   | 复杂   | 较低  |
| Rom体积    | 较小                 | Dalvik较大                   | 较小 | 较小   | 较小   | 大    |
| 成功率     | 高                   | 较高                         | 较高 | 一般   | 最高   | 较高  |
| 热度       | 高                   | 高                           | 低   | 低     | 高     | 低    |
| 开源       | no                   | yes                          | yes  | yes    | yes    | yes   |
| 收费       | 收费（设有免费阈值） | 收费（基础版免费，但有限制） | 免费 | 免费   | 免费   | 免费  |
| 监控       | 提供分发控制及监控   | 提供分发控制及监控           | no   | no     | no     | no    |

**即时生效**是我们的一个优先选择点，用户可能崩溃过一次，就不会打开第二次了，这期间会损失不少用户，所以这一点很重要。

**成功率**用户量比较多，机型也很杂，所以成功率高会优先选择

**免费！！！并且头条都在用**

以上三点基本上就定位了robust，虽然它不支持类替换、资源替换等功能，但是我们一般都是为了紧急处理线上崩溃才去使用热修复，基本上都是一些空指针、数据越界引起的，所以针对相关类去修复已经够用了，功能过于庞大反而失去了热修复的意义了，有点变相的迭代版本功能需求的味道。







#### 插件化

##### 基础

DroidPlugin 虽然不咋用，但是帮助理解原理

![image-20230714101021592](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20230714101021592.png)

**插件化解决的问题**

> 1.APP的功能模块越来越多，体积越来越大
> 2.模块之间的耦合度高，协同开发沟通成本越来越大
> 3.方法数日可能超过65535，APP占用的内存过大
> 4.应用之间的互相调用

**插件化与组件化的区别**

> 组件化开发就是将一个app分成多个模块，每个模块都是一个组件，开发的过程中我们可以让这些组件相互依赖或者单独调试部分组件等，但是最终发布的时候是将这些组件合并统一成一个apk,这就是组件化开发。
> 插件化开发和组件化略有不同。
>
> 插件化开发是将整个app拆分成多个模块，这些模块包括一个宿主和多个插件，每个模块都是一个apk,最终打包的时候宿主apk和插件apk分开打包。
>
> 前者参与打包，后者不参与；前者体积大，后者体积小；前者有bug的话组件不能动态改，后者插件有bug，可以动态修改下发。那为什么不用插件化代替热修复？（大材小用了，本来就要修复一个类里面代码，但是插件化换了整个）





##### **插件化实现思路分三步**

**第一步：如何加载插件的类？**
java部分Classloader看一遍先

![image-20230714111136680](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20230714111136680.png)

思路有了，接下来就是玩转反射！

```java
 public class LoadUtil {
        private final static String apkPath = "/sdcard/plugin-debug.apk";
        public static void loadClass(Context context) {
            /**
             * 宿主dexElements = 宿主dexElements + 插件dexElements
             * 1.获取宿主dexElements
             * 2.获取插件dexElements
             * 3.合并两个dexElements
             * 4.将新的dexElements 赋值到 宿主dexElements
             *
             * 目标：dexElements  -- DexPathList类的对象 -- BaseDexClassLoader的对象，类加载器
             *
             * 获取的是宿主的类加载器  --- 反射 dexElements  宿主
             *
             * 获取的是插件的类加载器  --- 反射 dexElements  插件
             */
            try {
                Class<?> clazz = Class.forName("dalvik.system.BaseDexClassLoader");
                Field pathListField = clazz.getDeclaredField("pathList");
                pathListField.setAccessible(true);

                Class<?> dexPathListClass = Class.forName("dalvik.system.DexPathList");
                Field dexElementsField = dexPathListClass.getDeclaredField("dexElements");
                dexElementsField.setAccessible(true);

                // 宿主的 类加载器
                ClassLoader pathClassLoader = context.getClassLoader();
                // DexPathList类的对象
                Object hostPathList = pathListField.get(pathClassLoader);
                // 宿主的 dexElements
                Object[] hostDexElements = (Object[]) dexElementsField.get(hostPathList);

                // 插件的 类加载器
                ClassLoader dexClassLoader = new DexClassLoader(apkPath, context.getCacheDir().getAbsolutePath(),
                        null, pathClassLoader);
                // DexPathList类的对象
                Object pluginPathList = pathListField.get(dexClassLoader);
                // 插件的 dexElements
                Object[] pluginDexElements = (Object[]) dexElementsField.get(pluginPathList);

                // 宿主dexElements = 宿主dexElements + 插件dexElements
								// Object[] obj = new Object[]; // 不行
                // 创建一个新数组
                Object[] newDexElements = (Object[]) Array.newInstance(hostDexElements.getClass().getComponentType(),
                        hostDexElements.length + pluginDexElements.length);

                System.arraycopy(hostDexElements, 0, newDexElements,
                        0, hostDexElements.length);
                System.arraycopy(pluginDexElements, 0, newDexElements,
                        hostDexElements.length, pluginDexElements.length);
                // 赋值
                // hostDexElements = newDexElements
                dexElementsField.set(hostPathList, newDexElements);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
```

**第二步：如何启动插件的四大组件？**

![image-20230714153443300](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20230714153443300.png)

启动Activity但是没有注册，如何绕过AMS检查，使用ProxyActivity

![image-20230714140316736](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20230714140316736.png)

```
try {
    intent.migrateExtraStreamToClipData(who);
    intent.prepareToLeaveProcess(who);
    int result = ActivityTaskManager.getService().startActivity(whoThread,
            who.getBasePackageName(), who.getAttributionTag(), intent,
            intent.resolveTypeIfNeeded(who.getContentResolver()), token,
            target != null ? target.mEmbeddedID : null, requestCode, 0, null, options);
    checkStartActivityResult(result, intent);
} catch (RemoteException e) {
    throw new RuntimeException("Failure from system", e);
}
```

```java
//从AMS跨进程启动act为hook点，修改intent为代理act（动态代理）
public class HookUtil {
    private static final String TARGET_INTENT = "target_intent";
    public static void hookAMS() {
        try {
            // 获取 singleton 对象
            Field singletonField = null;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) { // 小于8.0
                Class<?> clazz = Class.forName("android.app.ActivityManagerNative");
                singletonField = clazz.getDeclaredField("gDefault");
            } else {
                Class<?> clazz = Class.forName("android.app.ActivityManager");
                singletonField = clazz.getDeclaredField("IActivityManagerSingleton");
            }

            singletonField.setAccessible(true);
            Object singleton = singletonField.get(null);

            // 获取 系统的 IActivityManager 对象
            Class<?> singletonClass = Class.forName("android.util.Singleton");
            Field mInstanceField = singletonClass.getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);
            final Object mInstance = mInstanceField.get(singleton);
            Class<?> iActivityManagerClass = Class.forName("android.app.IActivityManager");
            // 创建动态代理对象
            Object proxyInstance = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                    new Class[]{iActivityManagerClass}, new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            // Intent的修改 -- 过滤
                            // 过滤
                            if ("startActivity".equals(method.getName())) {
                                int index = -1;

                                for (int i = 0; i < args.length; i++) {
                                    if (args[i] instanceof Intent) {
                                        index = i;
                                        break;
                                    }
                                }
                                // 启动插件的
                                Intent intent = (Intent) args[index];
                                Intent proxyIntent = new Intent();
                                proxyIntent.setClassName("com.enjoy.leo_plugin",
                                        "com.enjoy.leo_plugin.ProxyActivity");
                                proxyIntent.putExtra(TARGET_INTENT, intent);
                                args[index] = proxyIntent;
                            }
                            // args  method需要的参数  --- 不改变原有的执行流程
                            return method.invoke(mInstance, args);
                        }
                    });
            // ActivityManager.getService() 替换成 proxyInstance
            mInstanceField.set(singleton, proxyInstance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//AMS执行代理act生命周期时修改为真实act（反射）   新版本反射mInstrumentation去newactivity
    public static void hookHandler() {
        try {
            // 获取 ActivityThread 类的 Class 对象
            Class<?> clazz = Class.forName("android.app.ActivityThread");
            // 获取 ActivityThread 对象
            Field activityThreadField = clazz.getDeclaredField("sCurrentActivityThread");
            activityThreadField.setAccessible(true);
            Object activityThread = activityThreadField.get(null);
            // 获取 mH 对象
            Field mHField = clazz.getDeclaredField("mH");
            mHField.setAccessible(true);
            final Handler mH = (Handler) mHField.get(activityThread);
            Field mCallbackField = Handler.class.getDeclaredField("mCallback");
            mCallbackField.setAccessible(true);
            // 创建的 callback
            Handler.Callback callback = new Handler.Callback() {
                @Override
                public boolean handleMessage(@NonNull Message msg) {
                    // 通过msg  可以拿到 Intent，可以换回执行插件的Intent
                    // 找到 Intent的方便替换的地方  --- 在这个类里面 ActivityClientRecord --- Intent intent 非静态
                    // msg.obj == ActivityClientRecord
                    switch (msg.what) {
                        case 100:
                            try {
                                Field intentField = msg.obj.getClass().getDeclaredField("intent");
                                intentField.setAccessible(true);
                                // 启动代理Intent
                                Intent proxyIntent = (Intent) intentField.get(msg.obj);
                                // 启动插件的 Intent
                                Intent intent = proxyIntent.getParcelableExtra(TARGET_INTENT);
                                if (intent != null) {
                                    intentField.set(msg.obj, intent);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case 159:
                            try {
                                // 获取 mActivityCallbacks 对象
                                Field mActivityCallbacksField = msg.obj.getClass()
                                        .getDeclaredField("mActivityCallbacks");

                                mActivityCallbacksField.setAccessible(true);
                                List mActivityCallbacks = (List) mActivityCallbacksField.get(msg.obj);

                                for (int i = 0; i < mActivityCallbacks.size(); i++) {
                                    if (mActivityCallbacks.get(i).getClass().getName()
                                            .equals("android.app.servertransaction.LaunchActivityItem")) {
                                        Object launchActivityItem = mActivityCallbacks.get(i);

                                        // 获取启动代理的 Intent
                                        Field mIntentField = launchActivityItem.getClass()
                                                .getDeclaredField("mIntent");
                                        mIntentField.setAccessible(true);
                                        Intent proxyIntent = (Intent) mIntentField.get(launchActivityItem);

                                        // 目标 intent 替换 proxyIntent
                                        Intent intent = proxyIntent.getParcelableExtra(TARGET_INTENT);
                                        if (intent != null) {
                                            mIntentField.set(launchActivityItem, intent);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                    // 必须 return false
                    return false;
                }
            };
            // 替换系统的 callBack
            mCallbackField.set(mH, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

**第三步：如何加载插件的资源？**

**单独**给插件创建一个 Resource，不用宿主的Resource，很容易冲突报错等，addAssetPath

都是宿主的 context  --- 插件自己创建一个 context -- 绑定 启动插件资源的  Resource 

```java
public class MainActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        View view = LayoutInflater.from(mContext).inflate(R.layout.activity_main, null);
        setContentView(view);
    }
}

//BaseActivity
@Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Resources resources = LoadUtil.loadResource(getApplication());
        mContext = new ContextThemeWrapper(getBaseContext(), 0);
        Class<? extends Context> clazz = mContext.getClass();
        try {
            Field mResourcesField = clazz.getDeclaredField("mResources");
            mResourcesField.setAccessible(true);
            mResourcesField.set(mContext, resources);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //LoadUtil
      private static Resources loadResource(Context context) {
        // assets.addAssetPath(key.mResDir)
        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            // 让 这个 AssetManager对象 加载的 资源为插件的
            Method addAssetPathMethod = AssetManager.class.getMethod("addAssetPath", String.class);
            addAssetPathMethod.invoke(assetManager, apkPath);
            // 如果传入的是Activity的 context 会不断循环，导致崩溃
            Resources resources = context.getResources();
            // 加载插件的资源的 resources
            return new Resources(assetManager, resources.getDisplayMetrics(), resources.getConfiguration());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
```



参考360DroidPlugin(该开源项目已经停止维护 , 就适配到了 8.0 , 9.0 Android 系统无法运行)



##### **Shadow**(重点)

原理可以查看码牛b站https://www.bilibili.com/video/BV1am4y1U79f?p=6&spm_id_from=pageDriver&vd_source=c1394435455d8ffc298f8724a0fd3399

文档参考 shadow[框架分析](https://github.com/5A59/android-training/blob/master/common-tec/shadow%E6%A1%86%E6%9E%B6%E5%88%86%E6%9E%90.md)

以上插件化方案需要反射+hook，需要做版本兼容，比较麻烦，所以shadow现在唯一的插件化方案

![image-20230722110546139](https://raw.githubusercontent.com/AndroidJiang/HomePics/master/818/image-20230722110546139.png)

| module称号                   | module编译产品    | 终究产品方式      | 是否动态加载 | 代码运转所在进程     | 首要职责                                             |
| ---------------------------- | ----------------- | ----------------- | ------------ | -------------------- | ---------------------------------------------------- |
| sample-host                  | 可独立运转的apk   | 可独立运转的apk   | 否           | 主进程和插件进程均有 | 是对外发布的app                                      |
| sample-manager               | pluginmanager.apk | pluginmanager.apk | 是           | 主进程               | 装置、办理及加载插件                                 |
| sample-plugin/sample-app     | app-plugin.apk    | plugin.zip        | 是           | 插件进程             | 事务逻辑                                             |
| sample-plugin/sample-base    | base-plugin.apk   | plugin.zip        | 是           | 插件进程             | 事务逻辑，被app以compileOnly的办法依靠               |
| sample-plugin/sample-loader  | loader.apk        | plugin.zip        | 是           | 插件进程             | 插件的加载                                           |
| sample-plugin/sample-runtime | runtime.apk       | plugin.zip        | 是           | 插件进程             | 插件运转时的署理组件，如container activity（见下文） |

**资源 ID 抵触问题**

那么下一个问题，便是插件中必定也会有对资源的拜访。一般状况下，资源拜访会是相似下面的这样的方式：

```c
textView.setText(R.string.main_activity_info);
```

咱们对资源的拜访经过一个int值，而这个值是在apk的打包期间，由脚本生成的。这个值与详细的资源之间存在一一对应的关系。

由于插件和宿主工程是独立编译的，假如不修正分区，两者的资源或许存在抵触，这个时分就不知道应该去哪里加载资源了。

为了处理这个问题，Shadow修正了插件资源的id的分区。修正资源id并不杂乱，只需求一行代码就能够处理：

```arduino
additionalParameters "--package-id", "0x7E", "--allow-reserved-package-id"
```

反编译打包完结的apk，也很容易就能够发现，同一个资源的分区是不同的。宿主工程的是7f开头，而插件则是7e。



**Replugin的思路：**

Hack宿主的ClassLoader，使得体系收到加载ContainerActivity的恳求时，回来的是PluginActivity类。

由于PluginActivity本质上也是一个承继了android.app.Activity的类，经过向上转型为activity去运用，理论上不会存在什么问题。

Replugin的这个计划的问题之一，是需求在宿主apk中，为每一个插件的事务Activity注册一个对应的坑位Activity、。关于这点，咱们先看下ClassLoader load办法的签名：

```arduino
public abstract class ClassLoader {
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        ......
    }
}
```

能够看到，ClassLoader在loadClass的时分，收到的参数只要一个类名。这就导致，关于每个事务插件中的Activity，都需求一个ContainerActivity与之对应。在宿主apk中，咱们需求注册许多的坑位Activity。

别的，Replugin hack了加载class的进程，后边也不得不持续用Hack手段处理体系看到了未装置的Activity的问题。比如体系为插件Activity[初始化](https://www.6hu.cc/archives/tag/初始化)的Context是以宿主的apk初始化的，插件结构就不得不再去Hack修复。

**Shadow的思路**

Shadow则运用了另一种思路。已然对体系而言，ContainerActivity是一个实在注册过的存在的activity，那么就让这个activity发动起来。

一同，让ContainerActivity持有PluginActivity的实例。ContainerActivity将自己的各类办法，顺次转发给PluginActivity去完结，如onCreate等生命周期的办法。

Shadow在这儿所采用的计划，本质上是一种署理的思路。在这种思路中，事实上，PluginActivity并不需求实在承继Activity，它只需求承继一个与Activity有着相似的办法的接口就能够了。

Shadow的这个思路，一个ContainerActivity能够对应多个PluginActivity，咱们只需求在宿主中注册有限个有必要的activity即可。

而且，后续插件假如想要新增一个activity，也不是有必要要修正宿主工程。只要事务上答应，完全能够复用已有的ContainerActivity。



**Shadow是如何加载插件中的dex的**

```scss
new DexClassLoader(apkFile.getAbsolutePath(), oDexDir.getAbsolutePath(), null, ODexBloc.class.getClassLoader());
```

 **Shadow是如何加载资源包的**

```
val packageManager = hostAppContext.packageManager
        packageArchiveInfo.applicationInfo.publicSourceDir = archiveFilePath
        packageArchiveInfo.applicationInfo.sourceDir = archiveFilePath
        packageArchiveInfo.applicationInfo.sharedLibraryFiles = hostAppContext.applicationInfo.sharedLibraryFiles
        try {
            return packageManager.getResourcesForApplication(packageArchiveInfo.applicationInfo)
        } catch (e: PackageManager.NameNotFoundException) {
            throw RuntimeException(e)
        }
```

也是通过正常的packageManager生成的。但是是生成一个新的Resources，区别于宿主中正常使用的Resources。由于插件和宿主中是不通的Resources，所以不会出现资源ID冲突的问题。

**插件如何用Context**

```java
#PluginContainerActivity
delegate.setDelegator(this); //上下文塞給中转规则ShadowActivityDelegate，值mHostActivityDelegator

#ShadowActivityDelegate
  //create中实例化跳转的插件act，
  val pluginActivity = mAppComponentFactory.instantiateActivity(
                mPluginClassLoader,
                pluginActivityClassName,
                mHostActivityDelegator.intent
  )
  pluginActivity.setHostActivityDelegator(mHostActivityDelegator)  //插件act赋值宿主上下文        
```

**插件的setContentView等方法如何调用的**

```java
public void setContentView(View arg0) {
  hostActivityDelegator.setContentView(arg0);//宿主上下文
}
```

**宿主加载插件中view:**

> 1 宿主启动插件进程的act；
> 2 此act被添加进共享变量（方便插件进程拿到该act接口）
> 3 插件进程发送广播到宿主进程
> 4 宿主接受广播利用插件化启动插件service（类似启动act）
> 5 插件service中拿到共享变量act接口，执行接口方法addview





参考文章

https://www.6hu.cc/archives/181080.html

shadow踩坑记

必须clean -打插件-stop-start apk

- 混淆中有去除日志代码，注释掉

- 版本号写死

- min和shadow中min冲突，改最小为17

- 腾讯sdk中manifest打包冲突，最终包只有他们的manifest，删除class.jar中的manifest

- 去除不必要的receiver

- 统一认证sdk报错，注释

- 乐变sdk不能下载，去除

- flavorDimensions(*flavorDimensionList, 'type',"env")

- mainpageapplication中注释

- ```java
  if(ObjectUtils.isEmpty(packageName) || !packageName.equals(currentProcName)){
      LogPrint.d("@time", "---MainPageApplication---other process-"+ "pkg:"+packageName + "cuproc:"+currentProcName);
      return;
  }
  ```

- java.lang.NoClassDefFoundError: android.app.PictureInPictureParams     eventbus不适配shadow，改为源码集成

  https://github.com/Tencent/Shadow/issues/720

  ```java
  void moveToSuperclass() {
      if (this.skipSuperClasses) {
          this.clazz = null;
      } else {
          this.clazz = this.clazz.getSuperclass();
          String clazzName = this.clazz.getName();
          if (this.getClass().getClassLoader().getClass() != this.clazz.getClassLoader().getClass()) {
              this.clazz = null;
          }
      }
  }
  ```

- so更新隔离问题

- app转换问题，sdk中有些代码是靠反射获取application，如

  ```java
  public static Application getApp() {
      if (sApplication != null) {
          return sApplication;
      } else {
          try {
              Class var0 = Class.forName("android.app.ActivityThread");
              Object var1 = var0.getMethod("currentActivityThread").invoke((Object)null);
              Object var2 = var0.getMethod("getApplication").invoke(var1);
              if (var2 == null) {
                  throw new NullPointerException("u should init first");
              }
              sApplication = (Application)var2;
              return sApplication;
          } 
      }
  }
  ```

  因为该代码在插件中的sdk，所以会默认会被转换成ShadowApplication返回，如果sApplication为空，那么会走下面反射获取宿主的App，但是宿主App是真实App，最终会提示

  ```
  Caused by: java.lang.ClassCastException: com.extscreen.runtime.sample.App cannot be cast to com.tencent.shadow.core.runtime.ShadowApplication
  ```

  解决：反射赋值插件app給sApplication值，不让其走反射获取宿主App（因为插件app也会转成ShadowApplication）

  ```java
  				Class applicationUtils  =  ApplicationUtils.class;
          Field field  = null;
          try {
              field = applicationUtils.getDeclaredField("sApplication");
              field.setAccessible(true);
              field.set(applicationUtils, Utils.getApp());
              Log.e("ajiang", ApplicationUtils.getApp()+"");
          } catch (Exception e) {
              e.printStackTrace();
          }
  ```

- 报错：抽象方法没实现![image-20240802164653437](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20240802164653437.png)


  ```java
  java.lang.AbstractMethodError: abstract method not implemented
  at com.ncg.paas.dex.bridge.WrapperIPlayApi.init(WrapperIPlayApi.java)
  at com.ncg.paas.dex.api.IPlayApi.init(Unknown Source)
  at com.cmgame.gamehall.cloudgame.necgame.NcGameActivity.l0(NcGameActivity.java:8)
  ```

  网易aar中代码

  ![image-20240731144114233](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20240731144114233.png)

  ```java
  this.mApi.init(var1, var2, var3);
  //mApi赋值：  这个b就是mApi，是从apk中反射获取这个对象，因为这个apk不参与编译，
  						String var2 = this.a.c(var1, "ncg_2.4.2024040817.apk");
              try {
                  ClassLoader var3 = this.a.d(var1, var2);
                  d.a("classLoader:" + var3);
                  Class var4 = var3.loadClass("com.ncg.paas.dex.bridge.ApiBridge");
                  Method var5 = var4.getMethod("getIPlayApi");
                  var5.setAccessible(true);
                  this.b = (IWrapperPlayApi)var5.invoke((Object)null);
                  this.b.setId(c.a());
              } catch (Throwable var6) {
                  var6.printStackTrace();
              }						
  
  ```

  apk中代码如下（apk代码**不参与打包**，所以不会转成ShadowApplication）

  ```kotlin
  public class ApiBridge {
      private static final WrapperIPlayApi iApi = new WrapperIPlayApi();
      public static IWrapperPlayApi getIPlayApi() {
          return iApi;
      }
  }
  #WrapperIPlayApi
  public class WrapperIPlayApi implements IWrapperPlayApi {
      public void init(InitRequest request, Application app, final IInitCallback callback) {
         //...
      }
  }
  ```

  

  **解决一**：

  1. IWrapperPlayApi、IPlayApi不参与插件化改造，那么保持Application，那么传递进来必须得是Application

     ```java
     excludeClasses = ["com.ncg.paas.dex.api.IWrapperPlayApi","com.ncg.paas.dex.api.IPlayApi"]
     ```

  2. 要保证传递的是Application，那就得用宿主的Application，因为插件中全被改造成ShadowApplication

     ```kotlin
      						//获取宿主Application
     						Class var0 = null;
                 Object application = null;
                 var0 = Class.forName("android.app.ActivityThread");
                 Object var1 = var0.getMethod("currentActivityThread").invoke((Object)null);
                 application = var0.getMethod("getApplication").invoke(var1);
                 if (application == null) {
                     throw new NullPointerException("u should init first");
                 }
     					  //反射获取类，调用init方法
                 Class<?> iPlayApiClass = Class.forName("com.ncg.paas.dex.api.IPlayApi");
                 Method getInstanceMethod = iPlayApiClass.getDeclaredMethod("getInstance");
                 Object iPlayApiInstance = getInstanceMethod.invoke(null);
                 Method initMethod = iPlayApiClass.getDeclaredMethod("init", InitRequest.class, Class.forName("android.app.Application"), IInitCallback.class);
                 initMethod.setAccessible(true);
                 initMethod.invoke(iPlayApiInstance, initRequest,application,new IInitCallback() {
                     @Override
                     public void onInit(int code, String extra) {
                        Log.d("ajiang", "code:" + code + " extra:" + extra);
                     }
                 });
     ```

     发现代码能正常运行了，但是报了个初始化异常code

     ```
     code:9002 extra:Fail to dynamic dex
     ```

     走进源码发现是这里报错了，对象为空，根本原因是类找不到java.lang.NoClassDefFoundError:com/ncg/paas/dex/api/IWrapperPlayApi

     ```java
     public void init(@NonNull InitRequest var1, @NonNull Application var2, @NonNull IInitCallback var3) {
         this.dynamic.a(var2);
         this.mApi = this.dynamic.a();
         if (this.mApi != null) {
             this.mApi.init(var1, var2, var3);
         } else {
             var3.onInit(9002, "Fail to dynamic dex");
         }
     }
     ```

     ```java
     //插件aar中方法
     public void a(Context var1) {
         if (this.b == null) {
             		String var2 = this.a.c(var1, "ncg_2.4.2024040817.apk");//获取apk路径
                 ClassLoader var3 = this.a.d(var1, var2);//将context对应的classloader作为父classsloader，也就是创建的var3这个classloader只能访问apk中的类以及父classloader（如果是宿主的context，那么无法访问插件的类，WrapperIPlayApi就在插件中，所以导致传递不同的context会有不一样的效果）
                 Class var4 = var3.loadClass("com.ncg.paas.dex.bridge.ApiBridge");//ApiBridge在apk中
                 Method var5 = var4.getMethod("getIPlayApi");
                 var5.setAccessible(true);
                 this.b = (IWrapperPlayApi)var5.invoke((Object)null);
                 this.b.setId(c.a());
         }
     }
     //this.a.d(var1, var2);调用如下，var1的加载器作为父
      private ClassLoader e(Context var1, String var2) {
             String var3 = var1.getDir("ncg_2.4.2024040817.apk".replace(".", "_") + "_dex", 0).getAbsolutePath();
             return new e(var2, var3, var1.getApplicationInfo().nativeLibraryDir, var1.getClassLoader());
     }
     public class ApiBridge {
         private static final WrapperIPlayApi iApi = new WrapperIPlayApi();
         public static IWrapperPlayApi getIPlayApi() {
             return iApi;
         }
     }
     public class WrapperIPlayApi implements IWrapperPlayApi 
     ```

     > 此方法行不通！根本原因：aar中自定义apk的classloader，将宿主的context.getClassLoader作为父classloader，这就导致这个apk的classloader只能找到apk中的类以及宿主的类，找不到插件aar中的IWrapperPlayApi类。

  **解决二：**

    sdk方配合修改Application类型为为Context类型

  ```kotlin
  #aar中IPlayApi  
  public void init(@NonNull InitRequest var1, @NonNull Context var2, @NonNull IInitCallback var3) 
  #aar中IWrapperPlayApi
  void init(@NonNull InitRequest var1, @NonNull Context var2, @NonNull IInitCallback var3);
  #ncg_2.4.2024040817.apk中WrapperIPlayApi
  public void init(InitRequest request, Context app, final IInitCallback callback)
  ```

  

  

  

​		



​		













#### AOP

![image-20240115111753851](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20240115111753851.png)

> 注：AspectJ通常也用于.class->.dex之间

##### APT

注解处理器，ButterKnife、Dagger、ARouter都是用的apt在编译期生成对应代码





**AspectJ**

```
@Before("execution(* android.app.Activity.on**(..))")
public void onActivityCalled(JoinPoint joinPoint) throws Throwable {
    Log.d(...)
}
```



##### **JavaSsist**

shadow修改插件activity的父类用的此技术

**ASM**

robust插入代码两种都可选择



#### 编译提速

**gradle提速**

- 开启gradle缓存

  ```
  kapt {
      useBuildCache = true
  }
  
  org.gradle.caching=true
  ```

- 并行kapt

  ```
  kapt.use.worker.api=true
  ```

- kapt编译规
  避注解处理被略过的场景有

  - 项目的源文件没有改变
  - 依赖的改变是ABI(Application Binary Interface)兼容的，比如仅仅修改某个方法的方法体。

  ```
  kapt.include.compile.classpath=false
  ```

- 增量注解处理

  ```
  kapt.incremental.apt=true
  ```

**注解提速**

如项目中Router注解处理器，编译时在对应模块都会生成文件，每次都会重新生成，非常的耗时！

- 在`resources/META-INF/gradle/incremental.annotation.processors`下进行声明你属于那种增量处理器

  ```java
  com.migugame.router.RouterProcessor, isolating  //注解处理器的全限定名,类别
  org.gradle.EntityProcessor,aggregating
  ```

- 对应的注解处理器实现类中

  ```
   @Override
      public Set<String> getSupportedOptions() {
          HashSet<String> hashSet = new HashSet<>();
          hashSet.add("org.gradle.annotation.processing.aggregating");
          return hashSet;
      }
  ```

  





---



### 性能优化

LeakCanary

不做线上

####  启动优化

https://zhuanlan.zhihu.com/p/490378558?utm_id=0

**首先知道如何去计算时间，然后提供解决方案**

attachon-----onwindowfocuschanged

**方案汇总：**

**1.** **Arouter插件 节约2s**

**2.** **黑白屏主题**

**3.** **异步启动任务管理（重点图）**

**4.** **布局优化，**

> 1. AsyncLayoutInflate 不能用在有fragment的Act)，懒加载viewpager+fragment（比如我的页面viewstub）AsyncLayoutInflater 很容易出现锁的问题，甚至导致了更多的耗时
> 2. X2C  编译时注解，不用解析xml，不用反射，不用io 。缺点：兼容性有问题，需要改源码。如TextView-AppcompatTextView

**5.** **idlehandler空闲加载**

```
 Message next() {
		if (pendingIdleHandlerCount < 0
                        && (mMessages == null || now < mMessages.when)) {
        pendingIdleHandlerCount = mIdleHandlers.size();
		}
 		for (int i = 0; i < pendingIdleHandlerCount; i++) {
       final IdleHandler idler = mPendingIdleHandlers[i];         
       keep = idler.queueIdle();           
		}
}                 
```

**6.** **MultiDex字节方案（参考multidex章节）**



**问题1：100个线程执行完后进行打印**

> join()    wait/notify  juc?

**问题2：A线程有三步，B线程需等待A执行完第二步后执行**

> join()不可以，不管放哪都不行，得用wait/notify如下

<img src="https://raw.githubusercontent.com/AndroidJiang/HomePics/master/typora/image-20220722221225632.png" alt="image-20220722221225632" style="zoom: 70%;" />

 

**问题3：AB线程各三步，C线程需等待A第二步和B第二步都执行后再执行**

> 此时wait/notify不可以了，如下

```kotlin
public class Test {
    public static void main(String[] args) {
        Object lock1 = new Object();
        Object lock2 = new Object();
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("threa1-0");
                synchronized (lock1) {
                    lock1.notify();
                }
                System.out.println("threa1-1");
            }
        });
        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("threa2-0");
                synchronized (lock2) {
                    lock2.notify();
                }
                System.out.println("threa2-1");
            }
        });
        Thread thread3 = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (lock1) {
                    try {
                        System.out.println("lock1_start");
                        //lock1等待中，那么下面lock2的同步代码块不会执行，必须等lock1唤醒后才能执行，但是下面代码会一直wait，等不到线程2的notify了
                        // （反之先锁lock2，如果线程1执行完后，才lock2.notify,那么lock会一直wait）
                        lock1.wait();
                        System.out.println("lock1_end");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                synchronized (lock2) {
                    try {
                        System.out.println("lock2_start");
                        lock2.wait();
                        System.out.println("lock2_end");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("thread3");
            }
        });
        thread3.start();
        thread2.start();
        thread1.start();
    }
}
输出
lock1_start
threa2-0
threa2-1
threa1-0
threa1-1
lock1_end
lock2_start
```

 不过可以用一把锁完成，如下：

```java
public class Test2 {
    private static int count = 2;
    public static void main(String[] args) {
        Object lock1 = new Object();
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (lock1) {
                    System.out.println("threa1-0");
                    count--;
                    if(count == 0)
                        lock1.notify();
                    System.out.println("threa1-1");
                }
            }
        });
        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (lock1) {
                    System.out.println("threa2-0");
                    count--;
                    if(count == 0)
                        lock1.notify();
                    System.out.println("threa2-1");
                }
            }
        });
        Thread thread3 = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (lock1) {
                    try {
                        System.out.println("lock1_start");
                        lock1.wait();
                        System.out.println("lock1_end");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println("thread3");
            }
        });
        thread3.start();
        thread2.start();
        thread1.start();
    }
}
```

 但是以上如果叠加更多的任务，那么不好维护，会有很多wait，但是我们可以用CountDownLatch来实现上面的计数思路。



```java
public static void main(String[] args) {
    CountDownLatch countDownLatch = new CountDownLatch(2);
    Thread thread1 = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            countDownLatch.countDown();
        }
    });
    Thread thread2 = new Thread(new Runnable() {
        @Override
        public void run() {
            countDownLatch.countDown();
        }
    });
    Thread thread3 = new Thread(new Runnable() {
        @Override
        public void run() {
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
    });
    thread3.start();
    thread2.start();
    thread1.start();
}
```

 实战AppStartUp

<img src="https://raw.githubusercontent.com/AndroidJiang/HomePics/master/typora/image-20220722212421222.png" alt="image-20220722212421222" style="zoom:67%;" />

![](https://raw.githubusercontent.com/AndroidJiang/HomePics/master/typora/image-20220722212504408.png)

第一步：拓扑排序

建立顺序关系，建立表方便后续notify对应task

第二步：CountDownLunch

优化：1 添加多个任务，重复代码太多，可以参考arouter或者字节码插装

​			2 参考leakcanary，不需要application注册，contentprovider启动时机（小米面试），配置最后一任务，可以查出之前的task

<img src="https://raw.githubusercontent.com/AndroidJiang/HomePics/master/typora/image-20220724175757095.png" alt="image-20220724175757095" style="zoom:67%;" />

```
	    3 如果任务2是主线程执行，那么以上做法不够完美，3号会一直等在那，子线程和主线程分开add
```

可以参考源码更全源码https://github.com/idisfkj/android-startup

  

 **启动速度优化**

阿里巴巴 [历时1年，上百万行代码！首次揭秘手淘全链路性能优化（上）-阿里云开发者社区 (aliyun.com)](https://developer.aliyun.com/article/710466) alpha库

<img src="https://raw.githubusercontent.com/AndroidJiang/HomePics/master/typora/image131.png" alt="img" style="zoom:50%;" /> 





---







#### 内存优化

xxx2期内存

##### **内存泄漏**

内存泄漏常见场景
（1）**资源**对象没关闭造成的内存泄漏（如： Cursor、File等）
（2）全局集合类强引用没清理造成的内存泄漏（特别是 static 修饰的集合）
（3）接收器、监听器注册**没取消**造成的内存泄漏，如广播，eventsbus
（4）Activity 的 **Context** 造成的泄漏，可以使用 ApplicationContext
（5）单例中的**static**成员间接或直接持有了activity的引用
（6）非静态内部类持有父类的引用，如非静态**handler**持有activity的引用

LeakCanary---Profiler---MAT

匿名内部类 非静态内部类

> 解决本质：断开GcRoot

场景：  启动一个透明主题的activity，当前页面有个自定义view带动画，不可见时才会暂停， 动画执行导致idlehandler不执行，mnewactivity不会赋为空，主线程一直在更新ui没有空闲。（idle控制pause stop源码见https://mp.weixin.qq.com/s/9W4imOoZ5s4vlTVKyKIIHg）

![image-20220809223559866](https://raw.githubusercontent.com/AndroidJiang/HomePics/master/typora/image-20220809223559866.png)

**解决：注意原Activity的生命周期只会执行onPause，不会执行onStop，所以还是可见状态，其中的动画关闭时机不能放在onStop或者onVisiable中，得放在onPause中去关闭。**

内存泄漏三方工具：LeakCanary   KOOM   Matrix

`Shallow Size`是指实例**自身**占用的内存, 可以理解为保存该'数据结构'需要多少内存, 注意**不包括**它引用的其他实例

`Retained Size`是指, 当实例A被回收时, 可以**同时被回收**的实例的Shallow Size之和

**咪咕场景一：**

```java
#SplashActivity 
splashPresenter.requestSelfPicAd(new SplashAdDelegate.SplashSelfAdCallback() {
            @Override
            public void onSuccess(ArrayList<AdInfoBean> adInfoBean) {
                mSelfAdPicData = SplashAdDelegate.getInstance().findNextAd(adInfoBean, 1);
            }
}
```

匿名内部类会持有外部类引用，也就是持有了SplashActivity，而单例又持有匿名类，单例-mHttpRequest-builder-httpCallBack-callback匿名类-act，解决之法destroy中就是切断，`request = null`,看下图对比，前后少了20M左右，完美！（22003791）

![image-20231020173602704](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20231020173602704.png)

![image-20231020174742718](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20231020174742718.png)



**咪咕场景二：**

```java
#SplashPresenter
ipPresenter.getIp(new IPPresenter.IPPresenterCallBack() {
    @Override
    public void getIpAddressSuccess(String ip) {
        AdSdk.getInstance().setIpInfo(new OtherCustomInfo() {
            @Override
            public String getIpCountry() {
                return Flags.getInstance().ipCountry;
            }
        });
    }
});
```

此处代码乍看没啥问题，通过工具分析后发现发生了泄露！下面通过MAT工具，也可以直接用Profile查看

![image-20231123153706321](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20231123153706321.png)

发现引用链：SplashActivity—>SplashPresenter—>匿名内部类0—>匿名内部类1—>静态全局变量(GcRoots)，（目的是把newOtherCustomIinfo这个对象赋值給全局变量public static OtherCustomInfo otherInfo;）静态写法是在广告sdk中，所以我们只能从我们这开始切断引用，我们知道**匿名内部类会持有外部类引用**，所以可以改成**静态内部类**，如下：

```java
static class IPPresenterCallBack implements IPPresenter.IPPresenterCallBack {
    @Override
    public void getIpAddressSuccess(String ip) {
        AdSdk.getInstance().setIpInfo(new OtherCustomInfo() {
            @Override
            public String getIpCountry() {
                return Flags.getInstance().ipCountry;
            }
        });
    }
}
//调用
ipPresenter.getIp(new IPPresenterCallBack());
```

重新运行代码，发现不泄露了，完美！



小tips：

> 匿名内部类可以改为lambda表达式，可以规避内存泄漏风险！《[参考](https://mp.weixin.qq.com/s?__biz=MzA5MzI3NjE2MA==&mid=2650256051&idx=1&sn=9439f6df950be249c022b172174a864b&chksm=886343dcbf14cacad87644d99f93ce0b585f611ae302f34e40f94ea4b356ff45895890a94aac&scene=27)》

**匿名内部类和lambda区别**

>  Lambda 表达式中，访问外部变量时，编译器会将这些变量的值拷贝到 Lambda 表达式的内部，而不是持有对外部类的引用。这种方式避免了 Lambda 表达式对外部类的强引用，从而减少了内存消耗和提升了性能。
>
>  匿名内部类会持有对外部类的引用是因为在其生成的字节码中会包含对外部类的引用。匿名内部类是实现了一个接口或者继承了一个类，并且同时创建了一个对象的类，因此需要持有对外部类的引用以访问外部类的成员变量和方法。

**内部类为什么会持有外部类的引用**

内部类虽然和外部类写在同一个文件中，但是编译后还是会生成不同的`class`文件，其中内部类的构造函数中会传入外部类的实例，然后就可以通过`this$0`访问外部类的成员。（https://blog.csdn.net/cpcpcp123/article/details/122000663）







##### **内存抖动**

因为大量的对象被创建又在短时间内马上被释放。

解决本质：避免频繁创建对象

1. 代码中去除可以重复创建的对象、少创建对象，如OnDraw中对象尽量提出去

2. 对象池复用: 如：Message，Parcel，LruArrayPool（map）

```java
public static Message obtain() {//链表
    synchronized (sPoolSync) {
        if (sPool != null) {
            Message m = sPool;
            sPool = m.next;
            m.next = null;
            m.flags = 0; // clear in-use flag
            sPoolSize--;
            return m;
        }
    }
    return new Message();

```

```java
public static Parcel obtain() {//数组
    final Parcel[] pool = sOwnedPool;
    synchronized (pool) {
        Parcel p;
        for (int i=0; i<POOL_SIZE; i++) {
            p = pool[i];
            if (p != null) {
                pool[i] = null;
                if (DEBUG_RECYCLE) {
                    p.mStack = new RuntimeException();
                }
                return p;
            }
        }
    }
    return new Parcel(0);
}
```

```java
private <T> T getArrayForKey(Key key) {//map
  return (T) groupedMap.get(key);
}
//LruArrayPool思想就是你想获取一个长度为length 的 array ,我返回给你一个大于或者等于这个length 的array ,我们实际传入的是int 类型,在get 与 put 的过程中对象拆箱装箱的这个操作,也创建了非常多的对象,那么如何去优化他呢, int 作为key 的 Array ,没错 就是SparseArray 的思想，下面byte数组会用到
```

3. byte数组不能用链表池复用，因为有**不同大小**，所以参考Glide的LruArrayPool ，但是key为对象Int会有自动拆装箱过程，导致Integer对象倍增，android中有SpareArray和ArrayMap支持key为int，最终可以参考**SparseArray**双数组实现，但是它没有容器功能，需要自己实现，LruMap,TreeArray([参考享学](https://www.bilibili.com/video/BV13s4y1U79P?p=13&spm_id_from=pageDriver&vd_source=c1394435455d8ffc298f8724a0fd3399))

![image-20220809223303319](https://raw.githubusercontent.com/AndroidJiang/HomePics/master/typora/image-20220809223303319.png)

![image-20231019161712070](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20231019161712070.png)<img src="https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20240130181639503.png" alt="image-20240130181639503" style="zoom:50%;" />



##### MAT使用

- profiler中下载dump文件保存1.hprof   （**必须转换否则格式不支持**）

- ```java
  hprof-conv -z /Users/AJiang/1.hprof /Users/AJiang/11.hprof
  ```

- MAT中导入11.hprof

- 选择Histogram 直方图

- 过滤要排查的类，排除弱、虚、软应用，只剩下强引用

  ![image-20231023104915282](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20231023104915282.png)

<img src="https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20231023134041959.png" alt="image-20231023134041959" style="zoom: 33%;" />

<img src="https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20231023134902046.png" alt="image-20231023134902046" style="zoom:33%;" />





LeakCanary见源码分析



**大图如何加载**

**BitmapRegionDecoder**主要用于显示图片的某一块矩形区域，如果你需要显示某个图片的指定区域，那么这个类非常合适。

```
						InputStream inputStream = getAssets().open("tangyan.jpg");
            //获得图片的宽、高
            BitmapFactory.Options tmpOptions = new BitmapFactory.Options();
            tmpOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, tmpOptions);
            int width = tmpOptions.outWidth;
            int height = tmpOptions.outHeight;
            //设置显示图片的中心区域
            BitmapRegionDecoder bitmapRegionDecoder = BitmapRegionDecoder.newInstance(inputStream, false);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap bitmap = bitmapRegionDecoder.decodeRegion(new Rect(width / 2 - 100, height / 2 - 100, width / 2 + 100, height / 2 + 100), options);
            mImageView.setImageBitmap(bitmap);
```

然后配合手势进行不断加载显示区域



---



#### 卡顿优化

https://juejin.cn/post/7214635327407308859?searchId=2024101514023225F473DE1B608A9D0645#heading-7

卡顿原因：主线程执行繁重的UI绘制、大量的计算或IO等耗时操作。

从监测主线程的实现原理上，主要分为3大类：

1. 主线程Printer监测

   依赖主线程 Looper，监测每次 dispatchMessage 的执行耗时（BlockCanary）。

2. Choreographer帧率测量

   依赖 Choreographer 模块，监测相邻两次 Vsync 事件通知的时间差（LogMonitor）。

3. 字节码插桩

   ASM字节码插桩分析慢函数耗时，超过阈值上报观测平台（Matrix）。




> 反射操作setAppTracingAllowed

![image-20230817155036921](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20230817155036921.png)



**线上监控：BlockCanary** 实现检测

原理就是log，计算时间差，统计堆栈信息，但是不一定准哦

```
Looper.myLooper()?.setMessageLogging(LogPrinter(Log.ERROR, "$$$$$$$$$$$"));
```

<img src="https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20230817160247991.png" alt="image-20230817160247991" style="zoom:50%;" />





**Perfetto**工具使用（Systrace升级版）

**如何抓取文件？**

> 1. adb shell perfetto -o /data/misc/perfetto-traces/trace_file.perfetto-trace -t 10s sched freq idle am wm gfx view binder_driver hal dalvik camera input res memory
> 2. adb pull /data/misc/perfetto-traces/trace_file.perfetto-trace 推送到电脑
> 3. 打开网页，open file   https://ui.perfetto.dev/









------



#### 瘦身优化

**1.图片压缩**

推荐使用[tinypng](https://link.juejin.cn/?target=https%3A%2F%2Ftinypng.com%2F)在线压缩或者as中插件TinyPngPlugin，然后再图片转webp

**2.资源缩减、混淆**

```java
minifyEnabled true 
shrinkResources true
```

资源混淆AndroidResGuard

**3.Lint分析器**

lint 工具不会扫描 assets/ 文件夹、通过反射引用的资源或已链接至应用的库文件。此外，它也不会移

除资源，只会提醒您它们的存在。 **与资源缩减不同，这里点击删除，那就是把文件删了。**

**native优化**

<img src="https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20230814103801791.png" alt="image-20230814103801791" style="zoom:33%;" />

**4.so移除方案  **

ndk{abiFilters:}过滤，这个指令可以配置只打包你配置的so库，没有配置的就不打包，很灵活。

**Q1： 只适配了armeabi-v7a,那如果APP装在其他架构的手机上，如arm64-v8a上，会蹦吗？**

A: 不会，但是反过来会。
因为armeabi-v7a(32位)和arm64-v8a(64位)会向下兼容：

```
只适配armeabi的APP可以跑在armeabi,x86,x86_64,armeabi-v7a,arm64-v8上，('x86，x86_64'一般存于模拟器或特定rom)
只适配armeabi-v7a可以运行在armeabi-v7a和arm64-v8a，
只适配arm64-v8a 可以运行在arm64-v8a上
```

> **只适配armeabi**
> 优点:基本上适配了全部CPU架构（除了淘汰的mips和mips_64）
> 缺点：性能低，相当于在绝大多数手机上都是需要辅助ABI或动态转码来兼容
> **只适配 armeabi-v7a**
> 同理方案一，只是又筛掉了一部分老旧设备,在性能和兼容二者中比较平衡
> **只适配 arm64-v8**
> 优点: 性能最佳
> 缺点： 只能运行在arm64-v8上，要放弃部分老旧设备用户
>
> 大多数情况下我们可以只用一种armeabi-v7a，**但目前各大应用市场都要支持64位**，所以也会加上arm64-v8a。所以一般这样 abiFilters "armeabi-v7a", "arm64-v8a"，咱们项目就是，之前没有支持64，被小米打回
>
> Q1： 只适配了armeabi-v7a,那如果APP装在其他架构的手机上，如arm64-v8a上，会蹦吗？
> A: 不会，但是反过来会。

```
release { 
    ndk { abiFilters "armeabi", "armeabi-v7a", "arm64-v8a" 
    //有些观点是只留下armeabi即可，armeabi 目录下的 So 可以兼容别的平台上的 So， //但是，这样 别的平台使用时性能上就会有所损耗，失去了对特定平台的优化,而且近期国内的应 用市场也开始要求适配64位的应用了 } 
    }

```

**so移除优化版**

对于性能敏感的模块，使用到的 So放在 armeabi 目录当中，代码中来判断一下当前 CPU 类型来加载对应架构的 So 文件，如下

![image-20240906151227564](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20240906151227564.png)

```
if (TextUtils.equals(abi, "x86")) {
    // 加载特定平台的So  
} else {
    // 正常加载
}
```



**5.SoLoader**

主要的核心思想：除了首次启动外需要的so，其他借助于远程的so下发,so下发到本地，插件化思想，有需要借助

jvm来架加载so，有很多case需要考虑到比如下载时机、网络环境、线程进程，加载失败是否有降级策略等等。参考facebook开源的[SoLoader](https://link.juejin.cn/?target=https%3A%2F%2Fgithub.com%2Ffacebook%2FSoLoader)。

**6.抽插件动态加载**

详见插件化模块

**7.移除无用三方库**

引入后，后期可能功能下架，及时去除



除了以上通用方案，还可以考虑以下极致方案：

1.改用小程序、h5；

2.删减功能；

3.修改三方库源码，如arouter阉割版；

4.资源服务器化（小游戏）

5.图片着色器，使用`tint`，返回箭头黑色改base；

6.减少enum使用，会生成额外字节码，每个enum能减少1k左右

7.split apk分包，或者aab谷歌bundle分包(注释ndk{})

```
splits{
    abi {
        enable true
        reset()
        include 'armeabi-v7a', 'arm64-v8a'
    }
}
```





#### 网络优化

接口合并，减少请求，带宽，非首页启动延后



#### 电量优化

![image-20240321145852421](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20240321145852421.png)

---





#### ANR

https://zhuanlan.zhihu.com/p/599231190<img src="https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20240111091940728.png" alt="image-20240111091940728" style="zoom:50%;" />

https://juejin.cn/post/6940061649348853796

**ANR场景**

- Service Timeout:比如前台服务在20s内未执行完成，后台服务Timeout时间是前台服务的10倍，200s；
- BroadcastQueue Timeout：比如前台广播在10s内未执行完成，后台60s；
- ContentProvider Timeout：内容提供者,在publish过超时10s;
- InputDispatching Timeout: 输入事件分发超时5s，包括按键和触摸事件。

##### ANR触发流程

ANR触发流程大致可分为**2种**一种是Service、Broadcast、Provider定时爆炸触发ANR，另外一种是Input触发ANR。

###### 定时消息触发

1. 埋定时炸弹。
2. 拆炸弹。
3. 引爆炸弹

举例startService

**埋炸弹**

```text
//com.android.server.am.ActiveServices.java
private final void realStartServiceLocked(ServiceRecord r,
        ProcessRecord app, boolean execInFg) throws RemoteException {
    ......
    //发个延迟20s消息给AMS的Handler
    bumpServiceExecutingLocked(r, execInFg, "create");
    ......
    try {
        //IPC通知app进程启动Service，执行handleCreateService
        app.thread.scheduleCreateService(r, r.serviceInfo,
                mAm.compatibilityInfoForPackage(r.serviceInfo.applicationInfo),
                app.getReportedProcState());
    } catch (DeadObjectException e) {
    } finally {
    }
}
```

**拆炸弹**

```text
//android.app.ActivityThread.java
@UnsupportedAppUsage
private void handleCreateService(CreateServiceData data) {
    try {
        //1. 初始化Service
        service = packageInfo.getAppFactory()
                .instantiateService(cl, data.info.name, data.intent);
        ......
        service.attach(context, this, data.info.name, data.token, app,
                ActivityManager.getService());
        //2. Service执行onCreate，启动完成
        service.onCreate();
        mServices.put(data.token, service);
        try {
            //3. Service启动完成，需要通知AMS移除消息
            ActivityManager.getService().serviceDoneExecuting(
                    data.token, SERVICE_DONE_EXECUTING_ANON, 0, 0);
        } catch (RemoteException e) {
        }
    } catch (Exception e) {
    }
}
```

**引爆炸弹**

```text
//com.android.server.am.ActivityManagerService.java
final MainHandler mHandler;
final class MainHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
        case SERVICE_TIMEOUT_MSG: {
            //这个mServices是ActiveServices，执行无响应弹窗
            mServices.serviceTimeout((ProcessRecord)msg.obj);
        } break;
        }   
    }
}
```



###### input触发

input超时机制为什么是扫雷，而非定时爆炸？由于对于input来说即便某次事件执行时间超过Timeout时长，只要用户后续没有再生成输入事件，则不会触发ANR



##### ANR案例分析

**常见anr原因**

> 1. 主线程频繁进行耗时的IO操作：如数据库读写
>
> 2. 多线程操作的死锁，主线程被block
>
> 3. 主线程被Binder 对端block
>    Android 中的进程间通信使用 Binder 机制，如果应用正在等待远程 Binder 对象返回结果，但对端未响应，主线程可能会被阻塞。
>
> 4. System Server中WatchDog出现ANR
>    系统服务负责 Android 系统的关键功能，例如电源管理、网络管理等。如果系统服务出现问题或崩溃，可能会导致主线程被 System Server 的 WatchDog 阻塞。
>
> 5. service binder的连接达到上线无法和和System Server通信
>
> 6. 系统资源已耗尽（管道、CPU、IO）

1.查看main主线程，anr都是发生在ui线程的，**trace文件顶部的线程一般都是ANR元凶**

###### **主线程执行耗时操作**

```kotlin
//模拟主线程耗时操作,View点击的时候调用这个函数
fun makeAnr(view: View) {
    var s = 0L
    for (i in 0..99999999999) {
        s += i
    }
    Log.d("xxx", "s=$s")
}
```

```text
suspend all histogram:    Sum: 206us 99% C.I. 0.098us-46us Avg: 7.629us Max: 46us
DALVIK THREADS (16):
"main" prio=5 tid=1 Runnable
  | group="main" sCount=0 dsCount=0 flags=0 obj=0x73907540 self=0x725f010800
  | sysTid=32298 nice=-10 cgrp=default sched=1073741825/2 handle=0x72e60080d0
  | state=R schedstat=( 6746757297 5887495 256 ) utm=670 stm=4 core=6 HZ=100
  | stack=0x7fca180000-0x7fca182000 stackSize=8192KB
  | held mutexes= "mutator lock"(shared held)
  at com.xfhy.watchsignaldemo.MainActivity.makeAnr(MainActivity.kt:58)
  at java.lang.reflect.Method.invoke(Native method)
  at androidx.appcompat.app.AppCompatViewInflater$DeclaredOnClickListener.onClick(AppCompatViewInflater.java:441)
  at android.view.View.performClick(View.java:7317)
  at com.google.android.material.button.MaterialButton.performClick(MaterialButton.java:1219)
  at android.view.View.performClickInternal(View.java:7291)
  at android.view.View.access$3600(View.java:838)
  at android.view.View$PerformClick.run(View.java:28247)
  at android.os.Handler.handleCallback(Handler.java:900)
  at android.os.Handler.dispatchMessage(Handler.java:103)
  at android.os.Looper.loop(Looper.java:219)
  at android.app.ActivityThread.main(ActivityThread.java:8668)
  at java.lang.reflect.Method.invoke(Native method)
  at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:513)
  at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1109)
```

从日志上看，主线程处于执行状态，不是空闲状态，导致ANR了，说明com.xfhy.watchsignaldemo.MainActivity.makeAnr这里有耗时操作。

###### **主线程被锁阻塞**

```kotlin
fun makeAnr(view: View) {
    val obj1 = Any()
    val obj2 = Any()
    //搞个死锁，相互等待
    thread(name = "卧槽") {
        synchronized(obj1) {
            SystemClock.sleep(100)
            synchronized(obj2) {
            }
        }
    }
    synchronized(obj2) {
        SystemClock.sleep(100)
        synchronized(obj1) {
        }
    }
}
```

```kotlin
"main" prio=5 tid=1 Blocked
  | group="main" sCount=1 dsCount=0 flags=1 obj=0x73907540 self=0x725f010800
  | sysTid=19900 nice=-10 cgrp=default sched=0/0 handle=0x72e60080d0
  | state=S schedstat=( 542745832 9516666 182 ) utm=48 stm=5 core=4 HZ=100
  | stack=0x7fca180000-0x7fca182000 stackSize=8192KB
  | held mutexes=
  at com.xfhy.watchsignaldemo.MainActivity.makeAnr(MainActivity.kt:59)
  - waiting to lock <0x0c6f8c52> (a java.lang.Object) held by thread 22   //注释1
  - locked <0x01abeb23> (a java.lang.Object)
  at java.lang.reflect.Method.invoke(Native method)
  at androidx.appcompat.app.AppCompatViewInflater$DeclaredOnClickListener.onClick(AppCompatViewInflater.java:441)
  at android.view.View.performClick(View.java:7317)
  at com.google.android.material.button.MaterialButton.performClick(MaterialButton.java:1219)
  at android.view.View.performClickInternal(View.java:7291)
  at android.view.View.access$3600(View.java:838)
  at android.view.View$PerformClick.run(View.java:28247)
  at android.os.Handler.handleCallback(Handler.java:900)
  at android.os.Handler.dispatchMessage(Handler.java:103)
  at android.os.Looper.loop(Looper.java:219)
  at android.app.ActivityThread.main(ActivityThread.java:8668)
  at java.lang.reflect.Method.invoke(Native method)
  at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:513)
  at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1109)

"卧槽" prio=5 tid=22 Blocked  //注释2
  | group="main" sCount=1 dsCount=0 flags=1 obj=0x12c8a118 self=0x71d625f800
  | sysTid=20611 nice=0 cgrp=default sched=0/0 handle=0x71d4513d50
  | state=S schedstat=( 486459 0 3 ) utm=0 stm=0 core=4 HZ=100
  | stack=0x71d4411000-0x71d4413000 stackSize=1039KB
  | held mutexes=
  at com.xfhy.watchsignaldemo.MainActivity$makeAnr$1.invoke(MainActivity.kt:52)
  - waiting to lock <0x01abeb23> (a java.lang.Object) held by thread 1
  - locked <0x0c6f8c52> (a java.lang.Object)  
  at com.xfhy.watchsignaldemo.MainActivity$makeAnr$1.invoke(MainActivity.kt:49)
  at kotlin.concurrent.ThreadsKt$thread$thread$1.run(Thread.kt:30)

......
```

注意看，下面几行：

```text
"main" prio=5 tid=1 Blocked
  - waiting to lock <0x0c6f8c52> (a java.lang.Object) held by thread 22
  - locked <0x01abeb23> (a java.lang.Object)

"卧槽" prio=5 tid=22 Blocked
  - waiting to lock <0x01abeb23> (a java.lang.Object) held by thread 1
  - locked <0x0c6f8c52> (a java.lang.Object)
```

主线程的tid是1，线程状态是Blocked，正在等待0x0c6f8c52这个Object，而这个Object被thread 22这个线程所持有，主线程当前持有的是0x01abeb23的锁。而卧槽的tid是22，也是Blocked状态，它想请求的和已有的锁刚好与主线程相反。这样的话，ANR原因也就找到了：线程22持有了一把锁，并且一直不释放，主线程等待这把锁发生超时。在线上环境，常见因锁而ANR的场景是SharePreference写入。

> ps: 一般来说main线程处于BLOCK、WAITING、TIMEWAITING状态，基本上是函数阻塞导致的ANR，如果main线程无异常，则应该排查CPU负载和内存环境。

###### **CPU被抢占**

```text
CPU usage from 0ms to 10625ms later (2020-03-09 14:38:31.633 to 2020-03-09 14:38:42.257):
  543% 2045/com.test.demo: 54% user + 89% kernel / faults: 4608 minor 1 major //注意看这里
  99% 674/android.hardware.camera.provider@2.4-service: 81% user + 18% kernel / faults: 403 minor
  24% 32589/com.wang.test: 22% user + 1.4% kernel / faults: 7432 minor 1 major
  ......
```

可以看到，该进程占据CPU高达543%，抢占了大部分CPU资源，因为导致发生ANR，这种ANR与我们的app无关。

###### **内存紧张导致ANR**

如果一份ANR日志的CPU和堆栈都很正常，可以考虑是内存紧张。看一下ANR日志里面的内存相关部分。还可以去日志里面搜一下**onTrimMemory**，如果dump ANR日志的时间附近有相关日志，可能是内存比较紧张了。

```text
10-31 22:37:19.749 20733 20733 E Runtime : onTrimMemory level:80,pid:com.xxx.xxx:Launcher0
10-31 22:37:33.458 20733 20733 E Runtime : onTrimMemory level:80,pid:com.xxx.xxx:Launcher0
10-31 22:38:00.153 20733 20733 E Runtime : onTrimMemory level:80,pid:com.xxx.xxx:Launcher0
10-31 22:38:58.731 20733 20733 E Runtime : onTrimMemory level:80,pid:com.xxx.xxx:Launcher0
10-31 22:39:02.816 20733 20733 E Runtime : onTrimMemory level:80,pid:com.xxx.xxx:Launch
```

###### binder数据量过大

```java
07-21 04:43:21.573  1000  1488 12756 E Binder  : Unreasonably large binder reply buffer: on android.content.pm.BaseParceledListSlice$1@770c74f calling 1 size 388568 (data: 1, 32, 7274595)07-21 04:43:21.573  1000  1488 12756 E Binder  : android.util.Log$TerribleFailure: Unreasonably large binder reply buffer: on android.content.pm.BaseParceledListSlice$1@770c74f calling 1 size 388568 (data: 1, 32, 7274595)07-21 04:43:21.607  1000  1488  2951 E Binder  : Unreasonably large binder reply buffer: on android.content.pm.BaseParceledListSlice$1@770c74f calling 1 size 211848 (data: 1, 23, 7274595)07-21 04:43:21.607  1000  1488  2951 E Binder  : android.util.Log$TerribleFailure: Unreasonably large binder reply buffer: on android.content.pm.BaseParceledListSlice$1@770c74f calling 1 size 211848 (data: 1, 23, 7274595)07-21 04:43:21.662  1000  1488  6258 E Binder  : Unreasonably large binder reply buffer: on android.content.pm.BaseParceledListSlice$1@770c74f calling 1 size 259300 (data: 1, 33, 7274595)
```

###### binder 通信失败

```java
07-21 06:04:35.580 <6>[32837.690321] binder: 1698:2362 transaction failed 29189/-3, size 100-0 line 304207-21 06:04:35.594 <6>[32837.704042] binder: 1765:4071 transaction failed 29189/-3, size 76-0 line 304207-21 06:04:35.899 <6>[32838.009132] binder: 1765:4067 transaction failed 29189/-3, size 224-8 line 304207-21 06:04:36.018 <6>[32838.128903] binder: 1765:2397 transaction failed 29189/-22, size 348-0 line 2916
```

###### **系统服务超时导致ANR**

系统服务超时一般会包含**BinderProxy.transactNative**关键字，来看一段日志：

```text
"main" prio=5 tid=1 Native
  | group="main" sCount=1 dsCount=0 flags=1 obj=0x727851e8 self=0x78d7060e00
  | sysTid=4894 nice=0 cgrp=default sched=0/0 handle=0x795cc1e9a8
  | state=S schedstat=( 8292806752 1621087524 7167 ) utm=707 stm=122 core=5 HZ=100
  | stack=0x7febb64000-0x7febb66000 stackSize=8MB
  | held mutexes=
  kernel: __switch_to+0x90/0xc4
  kernel: binder_thread_read+0xbd8/0x144c
  kernel: binder_ioctl_write_read.constprop.58+0x20c/0x348
  kernel: binder_ioctl+0x5d4/0x88c
  kernel: do_vfs_ioctl+0xb8/0xb1c
  kernel: SyS_ioctl+0x84/0x98
  kernel: cpu_switch_to+0x34c/0x22c0
  native: #00 pc 000000000007a2ac  /system/lib64/libc.so (__ioctl+4)
  native: #01 pc 00000000000276ec  /system/lib64/libc.so (ioctl+132)
  native: #02 pc 00000000000557d4  /system/lib64/libbinder.so (android::IPCThreadState::talkWithDriver(bool)+252)
  native: #03 pc 0000000000056494  /system/lib64/libbinder.so (android::IPCThreadState::waitForResponse(android::Parcel*, int*)+60)
  native: #04 pc 00000000000562d0  /system/lib64/libbinder.so (android::IPCThreadState::transact(int, unsigned int, android::Parcel const&, android::Parcel*, unsigned int)+216)
  native: #05 pc 000000000004ce1c  /system/lib64/libbinder.so (android::BpBinder::transact(unsigned int, android::Parcel const&, android::Parcel*, unsigned int)+72)
  native: #06 pc 00000000001281c8  /system/lib64/libandroid_runtime.so (???)
  native: #07 pc 0000000000947ed4  /system/framework/arm64/boot-framework.oat (Java_android_os_BinderProxy_transactNative__ILandroid_os_Parcel_2Landroid_os_Parcel_2I+196)
  at android.os.BinderProxy.transactNative(Native method) ————————————————关键行！！！
  at android.os.BinderProxy.transact(Binder.java:804)
  at android.net.IConnectivityManager$Stub$Proxy.getActiveNetworkInfo(IConnectivityManager.java:1204)—关键行！
  at android.net.ConnectivityManager.getActiveNetworkInfo(ConnectivityManager.java:800)
  at com.xiaomi.NetworkUtils.getNetworkInfo(NetworkUtils.java:2)
  at com.xiaomi.frameworkbase.utils.NetworkUtils.getNetWorkType(NetworkUtils.java:1)
  at com.xiaomi.frameworkbase.utils.NetworkUtils.isWifiConnected(NetworkUtils.java:1)
```

从日志堆栈中可以看到是获取网络信息发生了ANR：getActiveNetworkInfo。系统的服务都是Binder机制（**16个线程**），服务能力也是有限的，有可能系统服务长时间不响应导致ANR。如果其他应用占用了所有Binder线程，那么当前应用只能等待。可进一步搜索：blockUntilThreadAvailable关键字：at android.os.Binder.blockUntilThreadAvailable(Native method)

如果有发现某个线程的堆栈，包含此字样，可进一步看其堆栈，确定是调用了什么系统服务。此类ANR也是属于系统环境的问题，如果某类型手机上频繁发生此问题，应用层可以考虑规避策略

###### 主线程无卡顿

```text
"main" prio=5 tid=1 Native
  | group="main" sCount=1 dsCount=0 flags=1 obj=0x74b38080 self=0x7ad9014c00
  | sysTid=23081 nice=0 cgrp=default sched=0/0 handle=0x7b5fdc5548
  | state=S schedstat=( 284838633 166738594 505 ) utm=21 stm=7 core=1 HZ=100
  | stack=0x7fc95da000-0x7fc95dc000 stackSize=8MB
  | held mutexes=
  kernel: __switch_to+0xb0/0xbc
  kernel: SyS_epoll_wait+0x288/0x364
  kernel: SyS_epoll_pwait+0xb0/0x124
  kernel: cpu_switch_to+0x38c/0x2258
  native: #00 pc 000000000007cd8c  /system/lib64/libc.so (__epoll_pwait+8)
  native: #01 pc 0000000000014d48  /system/lib64/libutils.so (android::Looper::pollInner(int)+148)
  native: #02 pc 0000000000014c18  /system/lib64/libutils.so (android::Looper::pollOnce(int, int*, int*, void**)+60)
  native: #03 pc 00000000001275f4  /system/lib64/libandroid_runtime.so (android::android_os_MessageQueue_nativePollOnce(_JNIEnv*, _jobject*, long, int)+44)
  at android.os.MessageQueue.nativePollOnce(Native method)
  at android.os.MessageQueue.next(MessageQueue.java:330)
  at android.os.Looper.loop(Looper.java:169)
  at android.app.ActivityThread.main(ActivityThread.java:7073)
  at java.lang.reflect.Method.invoke(Native method)
  at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:536)
  at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:876)
```

比如这个主线程堆栈，看起来很正常，主线程是空闲的，因为它正处于nativePollOnce，正在等待新消息。处于这个状态，那还发生了ANR，可能有2个原因：

1. dump堆栈时机太晚了，ANR已经发生过了，才去dump堆栈，此时主线程已经恢复正常了。
2. CPU 抢占：其他高优先级的任务或进程抢占了主线程的执行时间，导致主线程在等待 CPU 资源。
   内存紧张：系统内存不足，导致主线程无法继续执行，因为需要等待内存资源释放。

遇到这种情况，要先去分析**CPU、内存**的使用情况。其次可以关注**抓取日志的时间和ANR发生的时间**是否相隔太久，时间太久这个堆栈就没有分析的意义了。



总结：

1."ANR in"

2."locked" "wait"

3."onTrimMemory"

4."BinderProxy.transactNative"

anr正常在data/anr下，不可见，可以直接导出，或者adb shell命令查看

> adb pull /data/anr/traces.txt trace2.txt

##### 线上ANR处理

https://mp.weixin.qq.com/s/fWoXprt2TFL1tTapt7esYg

市面上很多都是模仿卡顿监控原理，也就是设置handler日志时间间隔为5s来监控anr，不严谨，只是input事件未被消费的默认值，其他场景也不是5s，而且如果没有后续input那么也不会导致anr。

比较完善的方案就是**微信Matrix**的处理方案，如何去监控线上ANR？

**1.监控SIGQUIT信号**

**充分非必要条件1：发生ANR的进程一定会收到SIGQUIT信号；但是收到SIGQUIT信号的进程并不一定发生了ANR。**

其他进程anr也可能会发送信号給你；非anr发送了该信号，开发者可以调用发送方法。

**2.NOT_RESPONDING**

**充分非必要条件2：进程处于NOT_RESPONDING的状态可以确认该进程发生了ANR。但是发生ANR的进程并不一定会被设置为NOT_RESPONDING状态。**

监控到SIGQUIT后，我们在20秒内（20秒是ANR dump的timeout时间）不断轮询自己是否有NOT_RESPONDING对flag，一旦发现有这个flag，那么马上就可以认定发生了一次ANR。

**3.主线程是否卡顿**

接收到SIGQUIT后，可能并不触发NOT_RESPONDING标记，但是也发生了ANR，如下面两种场景：

- **后台ANR（SilentAnr）：**之前分析ANR流程我们可以知道，如果ANR被标记为了后台ANR（即SilentAnr），那么杀死进程后就会直接return，并不会走到产生进程错误状态的逻辑。这就意味着，后台ANR没办法捕捉到，而后台ANR的量同样非常大，并且后台ANR会直接杀死进程，对用户的体验也是非常负面的，这么大一部分ANR监控不到，当然是无法接受的。
- **闪退ANR：**除此之外，我们还发现相当一部分机型（例如OPPO、VIVO两家的高Android版本的机型）修改了ANR的流程，即使是发生在前台的ANR，也并不会弹窗，而是直接杀死进程，即闪退。这部分的机型覆盖的用户量也非常大。并且，确定两家今后的新设备会一直维持这个机制。

解决方案：**反射过主线程Looper的mMessage对象，该对象的when变量，表示的就是当前正在处理的消息入队的时间，我们可以通过when变量减去当前时间，得到的就是等待时间，如果等待时间过长，就说明主线程是处于卡住的状态**，这时候收到SIGQUIT信号基本上就可以认为的确发生了一次ANR

总结：通过上面几种机制来综合判断收到SIGQUIT信号后，是否真的发生了一次ANR，最大程度地减少误报和漏报，才是一个比较完善的监控方案。

4.获取trace

通过socket的write方法来写Trace的，**我们能够hook到这里的write，就可以拿到系统dump的ANR Trace内容**





------





**在Activity#onCreate中sleep会导致ANR吗？**

> 不会，ANR的场景只有下面4种：Service Timeout、BroadcastQueue Timeout、ContentProvider Timeout、InputDispatching Timeout。
>
> 当然，如果在Activity#onCreate中sleep的过程中，用户点击了屏幕，那是有可能触发InputDispatching Timeout的。







------



#### 编译优化

参考超神https://juejin.cn/post/7344625554529730600

<img src="https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20240402104210246.png" alt="image-20240402104210246" style="zoom: 50%;" />

> • aapt 打包资源文件，产生R.java ，resource.arsc , res，manifest.xml
>
> • aidl 产生java接口文件
>
> • javac 编译R.java,Aidl产生的java文件以及工程中用到的源码产生.class文件
>
> • R8混淆和代码压缩.class文件，经过d8 生成.dex文件
>
> • apk builder 将资源包，.dex打包生成apk文件
>
> • apksigner /jarSigner 会对apk进行签名
>
> • ziplign 对apk 进行对齐操作，以便运行时节约内存。



- 不常改动module提前编译为aar，如kotlin编译耗时，提前生成aar，由implementation project更改为implementation aar

  拓展：如果module和aar中都有一个与主工程中同名的布局，那么在集成方式上，module和aar会有以下主要区别：

  1. **module**：如果你在项目中使用module，并且module中有一个与主工程中同名的布局，那么在编译时，Android的构建系统会按照一定的优先级来处理这种冲突。通常情况下，主工程的资源会覆盖module中的同名资源。这意味着，如果你在运行应用时，系统会使用主工程中的布局，而不是module中的布局。
  2. **aar**：如果你在项目中使用aar，并且aar中有一个与主工程中同名的布局，那么在编译时，Android的构建系统同样会按照一定的优先级来处理这种冲突。但是，因为aar是一个预编译的库，所以在处理资源冲突时，可能会有一些不同。具体的行为可能会依赖于你的构建系统和aar的创建方式。在某些情况下，aar中的资源可能会覆盖主工程中的同名资源。

  可参考GameDownloadProgressBar游戏详情页下载进度条问题

- kotlin增量编译

- 注解增量编译

最好分支打包，出数据对比

https://mp.weixin.qq.com/s/SJ-0We7e8kK3btsrBKkZ4g



今日头条：https://juejin.cn/post/6854573211548385294
b站：https://mp.weixin.qq.com/s/rDvOQWcfCC-P9Uu-IQzHhw







#### webview优化

 **为什么会慢？**

> 1）Webview初始化。
>
> 2）到达新的页面，网络连接，从服务器下载html，css，js，页面白屏。
>
> 3）页面基本框架出现，js请求页面数据，页面处于loading状态。
>
> 4）出现所需的数据，完成整个页面的渲染，用户可交互。

![image-20231008100922470](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20231008100922470.png)

方案：https://blog.csdn.net/u013038616/article/details/119138028

1. **提前初始化/WebView池**
   首次初始化和后续初始化时间差别很大，因为首次需要加载webview内核，内核是共享的，后续节省时间，所以我们可以在全局生成一个webview，使用时添加到rootview，注意清理工作等

2. **h5页面拉取**
   我们可以把公共html，css，js，image等资源预置在客户端本地，图片资源的拉取是最为耗时的，一个比较好的解决方案就是先加载并展示非图片内容，延迟这些图片的加载，以提升用户体验。

   WebView有一个setBlockNetworkImage(boolean)方法，该方法的作用是是否屏蔽图片的加载。可以利用这个方法来实现图片的延迟加载：在onPageStarted时屏蔽图片加载，在onPageFinished时开启图片加载 

```
mWebview.setWebViewClient(new WebViewClient() {
             //API21以下用shouldInterceptRequest(WebView view, String url)
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                if (url.contains("logo.gif")) {
                    InputStream is = null;
                    // 步骤2:创建一个输入流
                    try {
                        is =getApplicationContext().getAssets().open("images/error.png");
                    }
                    // 步骤4:替换资源
                    WebResourceResponse response = new WebResourceResponse("image/png",
                           "utf-8", is);
                    return response;
                }
                return super.shouldInterceptRequest(view, url);
            }
```

3. cache缓存

   ```java
   webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);//默认配置根据cache-control决定是否从网络上取数据。
   ```

4. 图片加载

   加载大量图片时候表现不佳，重复进入时还会重复加载图片

   WebViewClient#shouldInterceptRequest方法拦截WebView的资源加载，判断要加载的资源url是否为图片，是就走Glide加载并生成加载图片的WebResourceResponse





#### IO泄露检查

https://mp.weixin.qq.com/s/PCLig7XxJ4GAdEoEuUtN0Q （参考之matrix-io-canary）

通过动态代理+反射代理掉reporter这个静态变量，替换成我们自定义实现的Reporter接口的类，并在自定义类中实现io泄漏异常上报的逻辑

![image-20231226152657653](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20231226152657653.png)









**4.14知识点：**

**Viewpager（默认预加载）  Viewpager2(默认懒加载)**

**lazyfragment**

**适配器模式**

**布局优化 viewstub**

**okhttp网络可以暂停吗 执行中不能，等待直接删除**

 

 





### 难点

#### **IdleHandler**

**首先得知道这个IdleHandler回调不执行，会有啥危害？**

- onStop、onDestroy不能及时执行，基本都在十秒左右(系统兜底)，导致内存泄漏；
- 启动优化HomeActivity中空闲不执行，导致一些初始化代码不能正常执行，导致后续问题。

引起空闲handler不执行的根本原因就是主线程消息繁忙，总结有几下几种场景：

> - 在Handler.post(Runnable)中的Runnable中给自己发消息，也就是死循环发消息，且发的速度特别快
>
> - 绘制死循环，也就是在自定义View的onDraw方法中调用invalidate方法，或者global监听视图中一直刷新布局
>
> - 动画不及时释放
>
> - 布局嵌套导致的死循环　　
>
>   ```java
>   CoordinatorLayout+AppBarLayout+CollapsingToolbarLayout，需要去掉CollapsingToolbarLayout，不然主线程会处于半死机状态（主线程消息会非常繁忙）
>   最终将CollapsingToolbarLayout换成了FrameLayout，ps：原因未深究，猜测是因为布局签到死循环invalidate或者requestLayout导致的
>   猜测错误：CollapsingToolbarLayout会监听WindowInsets，回调中把它消费掉，并没有检查fitSystemWindows就直接消费，不妥。
>     消费意味着viewpager中
>   ```
>
>   ```java
>   去除根节点的android:fitsSystemWindows="true"属性，因为其会导致UI频繁的刷新，ps：原因未深究
>   ```

##### 小试牛刀

下面列举实际场景中会出现的问题：

1.内存泄漏场景   启动一个透明主题的activity，当前页面有个自定义view带动画，不可见时才会暂停， 动画执行导致idlehandler不执行，mactivitys没有赋值null，持有引用，也就会导致onStop、onDestroy无法正常执行

2.idlehandler 空闲线程不执行，切换到其他tab才会去执行（会关闭），loop中打印日志，发现挑战区有个自动滚动的列表，10ms就重新scrollto一次，一直绘制。

```java
Looper.myLooper().setMessageLogging(new LogPrinter(Log.DEBUG,LOG_TAG));

日志
<<<<< Finished to Handler (android.view.ViewRootImpl$ViewRootHandler) {c5afe55} cn.emagsoftware.gamehall.widget.recyclerview.AutoPollRecyclerView$AutoPollTask@e844865
 >>>>> Dispatching to Handler (android.view.ViewRootImpl$ViewRootHandler) {c5afe55} com.migugame.home_module.ui.adapter.recommend.HomeChallengeAdapter$7$1@dcfb12e: 0
```



##### 初露锋芒

升级到androidx后，发现不能通过上面的方式查看日志去定位，因为后台一直在打印编舞者信息，走的系统渲染看不出是谁在调用

```
 E  >>>>> Dispatching to Handler (android.view.Choreographer$FrameHandler) {fe83801} android.view.Choreographer$FrameDisplayEventReceiver@8b656a6: 0
 E  <<<<< Finished to Handler (android.view.Choreographer$FrameHandler) {fe83801} android.view.Choreographer$FrameDisplayEventReceiver@8b656a6
 E  >>>>> Dispatching to Handler (android.view.Choreographer$FrameHandler) {fe83801} android.view.Choreographer$FrameDisplayEventReceiver@8b656a6: 0
 E  <<<<< Finished to Handler (android.view.Choreographer$FrameHandler) {fe83801} android.view.Choreographer$FrameDisplayEventReceiver@8b656a6
 E  >>>>> Dispatching to Handler (android.view.Choreographer$FrameHandler) {fe83801} null: 2
 E  <<<<< Finished to Handler (android.view.Choreographer$FrameHandler) {fe83801} null
 E  >>>>> Dispatching to Handler (android.view.Choreographer$FrameHandler) {fe83801} android.view.Choreographer$FrameDisplayEventReceiver@8b656a6: 0
 E  <<<<< Finished to Handler (android.view.Choreographer$FrameHandler) {fe83801} android.view.Choreographer$FrameDisplayEventReceiver@8b656a6
 E  >>>>> Dispatching to Handler (android.view.Choreographer$FrameHandler) {fe83801} android.view.Choreographer$FrameDisplayEventReceiver@8b656a6: 0
 E  <<<<< Finished to Handler (android.view.Choreographer$FrameHandler) {fe83801} android.view.Choreographer$FrameDisplayEventReceiver@8b656a6
```

查找一番后发现是因为viewpager[无限绘制](https://www.jianshu.com/p/25c139e9666a)导致的，触发viewpager无限绘制场景：

> - 升级到androidx（测试发现appcompat:1.0.0无问题，appcompat:1.3.1以上有问题，参考KtProjectAll）
> - 使用了全屏（View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN）
> - 页面中有ViewPager
> - ViewPager中有CollapsingToolbarLayout
> - 根布局没有使用fitsSystemWindows

下面通过Perfetto和断点去分析这个问题。

> 手机开发者中开启系统跟踪，打开app，下拉通知点击停止跟踪
>
> adb pull /data/local/traces .      上传到电脑，Perfetto打开该文件

修改前：

![image-20231117152815133](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20231117152815133.png)

循环绘制dispatchApplyInsets，作为突破口，然后配合断点

![image-20231117155059104](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20231117155059104.png)

![image-20231117154741330](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20231117154741330.png)

**帧处理**：在 `Choreographer` 中调用 `doFrame` 方法，开始处理一帧的绘制。

**视图遍历**：`ViewRootImpl` 进入 `performTraversals`，执行视图的布局和绘制操作。

**请求窗口插入**：在遍历过程中，窗口插入变化被检测到，调用 `dispatchApplyInsets` 方法。

**分发插入**：`ViewPager` 的 `onApplyWindowInsets` 被调用，开始遍历其子视图（如 `CollapsingToolbarLayout`）。

**消费插入**：`CollapsingToolbarLayout` 消费窗口插入，并调用 `requestApplyInsets(view)` 请求重新应用插入。

**循环请求**：由于 `requestApplyInsets`，`ViewPager` 再次进入 `onApplyWindowInsets`，重新开始遍历其子视图。

**死循环**：`CollapsingToolbarLayout` 不断消费插入并请求重新应用，导致 `ViewPager` 不断触发插入请求，形成无限循环，导致死循环。

分析：

viewpager中监听insets变化，当全屏或者子布局消费insets都会触发回调，遍历子布局分发insets

```java
#viewpager 
ViewCompat.setOnApplyWindowInsetsListener(this,
                new androidx.core.view.OnApplyWindowInsetsListener() {
                  //遍历子布局分发
				final WindowInsetsCompat childInsets = ViewCompat
        		.dispatchApplyWindowInsets(getChildAt(i), applied);                
}
#CollapsingToolbarLayout                                        
  ViewCompat.setOnApplyWindowInsetsListener(this, new OnApplyWindowInsetsListener() {
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                return CollapsingToolbarLayout.this.onWindowInsetChanged(insets);
            }
}); 
 //自我消费
 WindowInsetsCompat onWindowInsetChanged(WindowInsetsCompat insets) {
        return insets.consumeSystemWindowInsets();
}
                                                
```



**解决方案：**

> 1.viewpager下的有问题布局中的根增加（不推荐，viewpager下可能有多个framgent的子布局，不好把控，详细见文末）拦截子布局消费
>
> ```java
> android:fitsSystemWindows="true"
> ```
>
> 2.重写viewpager，进行消费（推荐）不会分发子布局insets
>
> ```java
> return applied.replaceSystemWindowInsets(res.left,res.top,res.right,res.bottom).consumeSystemWindowInsets();
> ```
>
> 3.去除自带消费并且无用的CollapsingToolbarLayout



解决这个大问题后，发现idleHandler可以正常回调了！大功告成！后续验证又发现如下小问题，属于优化：

通过Perfetto查看：进入到活动列表后，有动画仍然在执行，但是页面上没有动画知道是动画引起的，在对应源码位置断点（只能断方法，查看左侧调用链），定位出资源id即可查找出问题所在。

![image-20231117160433075](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20231117160433075.png)

![image-20231117163131466](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20231117163131466.png)

改完继续测，又发现一个静态页面，log仍然在刷，perfetto也一直循环，同样可以定位出问题。

![image-20231117164746646](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20231117164746646.png)



##### 大展身手

如果以上工具无法继续定位了，比如出现下面这种一直循环，但是又不知道是谁触发，可以通过下面**MethodTrace**工具定位：

参考https://www.codenong.com/cs110533341/



![image-20231117180040808](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20231117180040808.png)



app退到后台或者跳转到一个静态页面后，抓取trace，发现仍然在一直刷动画，配合断点找到问题控件

![image-20231117172749101](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20231117172749101.png)

![image-20231117172647156](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20231117172647156.png)



##### 总结

1. 项目中的动画合理使用，不提前开启，不可见及时停止，handler合理使用；

2. 不建议使用fitSystem去解决问题，不可控，后续viewpager中的子页面可能有修改或者新增，都会再次引起问题，如项目中首页测试过程中轻竞技tab的根布局添加fitSystem后测试通过，但是隔了一个版本发现idle再次出现问题，偶先不执行，通过工具查询：![image-20231122154532338](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20231122154532338.png)奇怪，怎么还是dispatchAPplyInsets方法的执行，配合断点定位：![image-20231122155133786](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20231122155133786.png)发现一起玩tab下顶部也有个viewpager，那就意味着viewpager下		不仅仅CollapsingToolbarLayout不消费，同样遇到viewPager	也会出现此问题，要么在这个根布局下再添加fitSystem，但是这样后		续还是容易出现其他地方问题，所以推荐使用下面方法。

3. 重写viewpager

   ```java
   return applied.replaceSystemWindowInsets(res.left,res.top,res.right,res.bottom).consumeSystemWindowInsets();
   ```

4. 如果CollapsingToolbarLayout没有作用就不要写，这也是我的最终解决方案，这样就不用重写viewpager，也不用每个布局加fitSys。







---









###  简历

 

熟悉APT Aspectj ASM Javaassist编译时技术

 

<img src="https://raw.githubusercontent.com/AndroidJiang/HomePics/master/typora/image208.png" alt="img" style="zoom:80%;" /> 

<img src="https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20240130102846927.png" alt="image-20240130102846927" style="zoom:50%;" />

<img src="https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20240130103310409.png" alt="image-20240130103310409" style="zoom:50%;" />

 

<img src="https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20240130102913921.png" alt="image-20240130102913921" style="zoom:50%;" />

<img src="https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20240130103500725.png" alt="image-20240130103500725" style="zoom:50%;" />

<img src="https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20240130103533160.png" alt="image-20240130103533160" style="zoom:50%;" />



简历二

<img src="https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20240130110055084.png" alt="image-20240130110055084" style="zoom:50%;" />







参考阿里，封装启动框架



项目看点：

1 直播悬浮窗逻辑

2 service



------

### Face

1. compileSDK 和 targetSDKVersion和buildToolsVersion区别

   > **`compileSdkVersion`**：
   >
   > - **目的**：定义编译时使用的 Android API 版本。
   > - **影响**：决定你的代码可以使用哪些 API。
   >
   > **`buildToolsVersion`**：
   >
   > - **目的**：定义构建应用时使用的工具版本。
   > - **影响**：控制编译、打包、签名等工具的版本。
   >
   > **`targetSdkVersion`**：
   >
   > - **目的**：定义应用希望在其上运行的 Android 版本。
   > - **影响**：系统根据这个版本来调整应用的运行行为和兼容性。

2. 说一下今日头条屏幕适配的原理（https://blog.51cto.com/u_16099193/8605562）

   > px = dp*density    density = dpi/160   dp = px/density ，要想所有设备dp相等，那么需要修改density值，比如：
   >
   > 设备1：分辨率1080x1920，dpi为480，要想屏幕总宽度dp=px/density=1080/3=360，那么density必须等于3；
   >
   > 设备2：分辨率1440x2560，dpi为560，要想屏幕总宽度dp=px/density=1440/4=360，那么density必须等于4
   >
   > 核心代码
   >
   > ```java
   > private static final float  WIDTH = 360;//参考设备的宽，单位是dp （UI）
   > private static float appDensity;//表示屏幕密度
   > private static float appScaleDensity; //字体缩放比例，默认appDensity
   > public static void setDensity(final Application application, Activity activity){
   > //获取当前app的屏幕显示信息
   > DisplayMetrics displayMetrics = application.getResources().getDisplayMetrics();
   > if (appDensity == 0){
   >    appDensity = displayMetrics.density;
   >    appScaleDensity = displayMetrics.scaledDensity;
   > }
   > //计算目标值density, scaleDensity, densityDpi
   > float targetDensity = displayMetrics.widthPixels / WIDTH; // 1080 / 360 = 3.0
   > float targetScaleDensity = targetDensity * (appScaleDensity / appDensity);
   > int targetDensityDpi = (int) (targetDensity * 160);
   > //替换Activity的density, scaleDensity, densityDpi
   > DisplayMetrics dm = activity.getResources().getDisplayMetrics();
   > dm.density = targetDensity;
   > dm.scaledDensity = targetScaleDensity;
   > dm.densityDpi = targetDensityDpi;
   > }
   > ```
   >
   > 缺点：三方库或者一些系统的不是按照ui图来，统一适配会有问题！
   >
   > 解决：按activity为单位，不该适配的不适配

3. 说一下组件化路由表底层怎么做的

4. 说一下隐私合规你们改了哪些内容

   > 用户同意协议之前，不得获取用户任何个人信息
   >
   > 用户同意协议之前，不得申请用户权限
   >
   > 
   >
   > 

5. 屏幕适配dp和sp有什么区别, dimens干什么用的

6. 说一下你们公司降级SDK怎么实现的?

7. 你们一周发几个版本, 怎么保证一周两个版本不影响客户

8. 说一下白屏监测原理

9. 说一下RecycleView三级缓存

10. 说一下R8和D8的区别
    ![image-20230914170506451](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20230914170506451.png)

    D8:脱糖 + 将.class字节码转换成dex就好

    R8:java代码压缩、资源压缩、调D8

    

11. 说一下如何对不同手机进行等级划分

12. 说一下ASM、AspectJ和AST的区别

13. 说一下你们项目的参数配置系统设计流程

14. 如何自定义一个gradle Plugin

小米 面
一面：
glide缓存 
glide设置565有效吗
glide和其他图片库如何设计切换 
几种图片库选择 根据哪些因素（就是区别）
okhttp源码 穿插问问题
性能优化  启动优化等
小米应用市场怎么去更新app  选择时间段  

> 1. **定期任务调度**使用`WorkManager`或者`AlarmManager`设置周期性任务以定期检查更新。`WorkManager`是推荐的方式，因为它考虑到了设备的电量和系统优化。例如：
>
> ```java
> PeriodicWorkRequest checkUpdateWorkRequest =
>         new PeriodicWorkRequest.Builder(UpdateWorker.class, 1, TimeUnit.DAYS)
>                 .build();
> WorkManager.getInstance(context).enqueueUniquePeriodicWork(
>         "check_for_update",
>         ExistingPeriodicWorkPolicy.KEEP,
>         checkUpdateWorkRequest);
> ```
>
> 2. **后台服务**在Android O之后启动周期性的后台服务有所限制，但你可以使用`JobIntentService`或者`JobScheduler`来执行周期性的任务。

手写代码 string转int  
你常用的设计模式
说一下观察者模式 
内存泄露场景  handler如何避免  oom
平时如果解决bug的 
为什么如此安全的https协议却仍然可以被抓包呢？

二面：
volite和sysnch
ipc几种方式  
intent传递大小有限制吗

> 通常1M-8k

activity之间传递大数据怎么处理

> 1. **全局单例**：
>    创建一个全局单例类来临时存储大数据，在需要的地方通过这个单例访问数据，但要格外注意内存泄露的问题。
> 2. **使用文件**：
>    将数据写入到文件系统中，然后传递文件路径给另一个`Activity`。在新的`Activity`中从该文件路径中读取数据。
> 3. **使用数据库**：
>    如果数据已经存储在本地数据库，只需传递数据的标识符（如数据库中的ID），在新的`Activity`中使用这个ID来查询数据。
> 4. **使用内容提供者（ContentProvider）**：
>    如果数据由自己的应用或者其他应用提供，可以通过实现一个`ContentProvider`来共享数据。

contentprovider
applicaton和provider的oncreate先后执行关系
glide三级缓存  placehold error fockback几个区别  后两个不设置 图片请求错误 怎么展示
okhttp拦截器几个说一下 拦截器中interceptor和networkinteceptor区别  重定向后在哪个里面返回
oom如何分析  mat工具如何使用等等 问的比较深
1<-2<-3  任务2依赖1结果 3依赖2结果   1 2都是异步  如何做
后台定时任务如何去做 如何在某个时间点做任务
 service8.0问题  
子线程handler的looper在哪
jetpack了解吗



小米（王均友）

1、app性能优化，先说说启动优化
 appstartup原理， 启动优化评测纬度或者说技术手段，除了打日志，启动场景分为哪些场景，热启动，冷启动，温启动
页面优化怎么做的？
 掉帧分析，app的卡顿，耗时，blockcanary原理，有没有自己根据需求定制
2、Android 跨进程通信，为什么会用binder
 binder的服务管理
3、view的绘制流程
4、网络优化，为什么要做网络优化，具体做了什么网络优化
5、recyclerview整个复用机制，里面的绘制机制，layoutmanager几个步骤，每个步骤里面大概做了什么
6、应用层绘制滞后卡顿，从系统整体上去解释一下
7、除了binder，还有哪些跨进程机制，操作系统层面来说
 socket到底是一个什么样的东西
8、链表排序（力扣148）



【2023-05-31模拟面试题】
1简单自我介绍
2三个项目中哪个最有技术挑战
3青模项目中有没有使用抖音中台提供的插件化框架
4做插件化这件事上有什么收获
5下包的流程，比如说怎么构建出来一个插件包
6那你知道插件化使用的是AAB bundle方案还是hook方案还是其他方案

> AAB是Google推出的一种新的发布格式，主要应用于应用发布阶段；而Hook技术则更多地应用于运行时的插件化实现。开发者选用哪种方案，通常取决于具体的应用场景和需求。

7下发的是一个个dex文件吗
8当我们打出来一个APK,有没有包含Activity这个类（正常打包，Framework的Activity)或者说Service
会打到我们的APK

> 不包含。Framework 层的类不会被打包进 APK 的主要原因是，这些类已经存在于 Android 设备的操作系统中。Android 操作系统的核心组件和框架层类是在设备的系统镜像中预装的，并且是由设备制造商或 Android 官方提供的。

9 Activity没有打包到APK,为什么我们可以使用到这个类呢

> 当我们在应用程序中使用 Activity 类时，我们实际上是利用了 Android 系统提供的 Activity 类的功能和特性。Android 框架负责管理 Activity 的生命周期、用户界面交互和其他与 Activity 相关的功能。这些功能和特性是通过 Android 操作系统中的 Framework 层实现的。

10OOM崩溃
11有处理过内存优化相关的事情没
12内存优化掌握到什么程度
13哪一个方面或技术领域研究的多一些
14关于网络这一块的理解
15 OKHttpi源码了解哪些
16 OKHttp设计的精髓在哪里
17简单说下OKHttp一个网络请求的流程
18 OKHttp的缓存设计
9能具体说一下服务端返回没有更新，服务端怎么判断客户端的结果有没有更新
20 OKHttp连接池作用是什么
21假设一个请求百度API,一个请求腾讯APi,连接池可以复用吗

> 当你使用 OkHttp 发送请求到不同的域名（例如百度和腾讯），实际上是使用了不同的主机名。在这种情况下，OkHttp 会为每个不同的主机名维护一个独立的连接池。这意味着不同域名的请求会使用不同的连接池，连接不会跨域名进行复用。

22连接池怎么复用
23网络相关的优化
24如何实现HTTPS建连，回传像HTTP一样快（连接复用没有生效时怎么做到呢，能不能通过空间换时间
实现)
25有没有做过弱网优化

> 1.设置超时；2.适当重试，错误反馈，监听网络恢复的时候重新请求；3.数据压缩；4.离线缓存数据

26拿到ANR trace,文件很大，怎么入手分析ANR
27ANR堆栈不明确，怎么定位原因
28多线程并发在实际开发中用到多吗
29 HashMap:是线程安全的吗
30多线程下使用HashMap,如何保证线程安全
31 ConcurrentHashMap如何保证线程安全
32 ConcurrentHashMap:去get元素的时候有加锁吗

> get 方法不需要加锁。因为 Node 的元素 value 和指针 next 是用 volatile 修饰的，在多线程环境下线程A修改节点的 value 或者新增节点的时候是对线程B可见的。
> 这也是它比其他并发集合比如 Hashtable、用 Collections.synchronizedMap()包装的 HashMap 效率高的原因之一。

33经常翻墙学习国外新技术，学了啥
34有解决过崩溃相关问题吗
35 Object finalize了解吗，只要有GC就会调用吗
36 Object notify了解吗，自己平时写代码有用过吗
37反射性能耗在哪里

> ①　invoke方法参数是object，比如原来方法是int，则装箱，然后再拆箱
>
> ②　getdecaleredmethod要循环遍历，费时间
>
> ③　检查方法可见性
>
> ④　编译器无法进行动态代码的优化，比如内联



虾哥面小红书

一面

- 自我介绍

- 看你写了很多文章，拿你理解最深刻的一篇出来讲一讲讲了 Binder 相关内容

- Binder 大概分了几层

- 哪些方法调用会涉及到 Binder 通信

- 大概讲一下 startActivity 的流程，包括与 AMS 的交互

- 全页面停留时长埋点是怎么做的我在项目中做过的内容，主要功能是计算用户在每个 Activity 的停留时长，并且支持多进程。这里的多进程支持主要是通过以 ContentProvider 作为中介，然后通过 ContentResolver.call 方法去调用它的各种方法以实现跨进程

- 动态权限申请是什么详见 Android 动态权限申请从未如此简单 这篇文章

- 你做的性能监测工具，FPS 是怎么采集的

- 性能监测工具用在了什么场景

- 有没有通过这个性能监测工具去做一些优化

- 图片库，例如 Glide，一般对 Bitmap 有哪些优化点

- 过期的 Bitmap 可以复用吗

- 有没有基于 ASM 插桩做过一些插件

- 讲了一下当时做过的一个个人项目 FastInflate这个项目没能达到最终的目标，但通过做这个项目学习了很多新知识，比如 APT 代码生成、阅读了 LayoutInflater 源码、AppCompatDelegateImpl 实现的 LayoutInflater.Factory2 会极大的拖慢布局创建的速度等

- 怎么优化布局创建速度提示了预加载，但我当时脑抽在纠结 xml 的缓存，没想到可以提前把视图先创建好

- 说一下你觉得你最擅长或者了解最透的点我回答的自定义 View

- 解决过 View 的滑动冲突吗

- 讲解了一个之前写过的开源控件 SwipeLoadingLayout

- 一般遇到困难的解决方案是什么

- 算法题：反转链表

- 反问阶段

- - 咱们组主要负责哪些内容
  - 主要使用 Java 还是 Kotlin
  - 小红书的面试一般是怎么个流程？多少轮？一般三轮技术面，一轮 HR 面
  - 面试完一般多久会给到结果比较快，一两天的样子



二面

- 为什么这个时间节点想要出来换工作呢

- 在 B 站这些年做了什么

- 做了哪些基础组件讲解了一下之前写的 SwipeLoadingLayout

- 介绍一下 Android 的事件传递机制

- 你写的这个分享模块是如何设计的对外采用流式调用的形式，内部通过策略模式区分不同的平台以及分享类型，给每个平台创建了一个中间 Activity 作为分享 SDK 请求的发起方（SDK.getApi().share()）以及分享结果的接收方（onActivityResult），然后通过广播将分享的结果送入到分享模块内进行处理，最终调用用户设置的分享回调告知结果

- 看你之前在扇贝的时候有开发过一些性能监测工具，那有做过性能优化吗

- 你是如何收集这些性能数据的

- 有没有对哪方面做过一些针对性的优化

- Android 系统为什么会触发 ANR，它的机制是什么

- 有解过 ANR 相关的问题吗？有哪几种类型？

- 算法题：二叉树的层序遍历

- Queue 除了 LinkedList 还有哪些实现类

- 现在还在面其他公司吗？你自己后面职业生涯的选择是怎么样的？

- 给我介绍了一下团队，说我面试的这个部门应该说是小红书最核心的团队，包括主页、搜索、图文、视频等等都在部门业务范畴内，部门主要分三层，除了业务层之外还有基础架构层以及性能优化层

- 反问阶段

- - 部门分三层的话，那新人进来的话是需要从业务层做起吗？不是这样的，我们首先会考虑这个同学能干什么，然后会考虑这个同学愿意去做什么，进来后，有经验的同学也会来带你的，不会一上来就让你抗输出，总之会把人放到适合他的团队里
  - 小红书会使用到一些跨端技术吗？会，之前在一些新的 App 上使用的 Flutter，现在主要用的是 RN，还会使用到一些 DSL，这个不能算跨段。为什么在小红书社区 App 中跨端技术提及的比较少，是因为小红书 App 非常重视用户体验，对性能的要求比较高

三面

- 介绍一下目前负责的业务
- 工作过程中有碰到过什么难题，最后是怎么解决的一开始脑抽了没想到该说什么，随便扯了一个没啥技术含量的东西，又扯了一个之前做的信号捕获的工具，后来回忆起来了，重新说了一个关于 DEX 编排的东西（主 DEX 中方法数超过 65535 导致打包失败，写了个脚本将一部分 Class 从主 DEX 中移除到其他 DEX 中）
- 如何设计一个头像的自定义 View，要求使头像展示出来是一个圆形
- 介绍一下 Android 事件的分发流程
- 如何处理 View 的防误触
- 怎么处理滑动冲突
- Activity 在 onCreate 方法中调用了 finish 方法，那它的生命周期会是怎样的
- 如果我想判断一个 Activity 中的一个 View 的尺寸，那我什么时候能够拿到
- RecyclerView 如何实现一个吸顶效果
- Java 和 Koltin 你哪个用的比较多
- 有用过 Kotlin 的协程吗
- Kotlin 中的哪些 Feature 你用的多，觉得写的好呢
- 你是怎么理解 MVVM 的
- 你有用过 Jetpack Compose 吗
- 有用过 kotlin 中的 by lazy 和 lateinit 吗
- kotlin 中怎么实现单例，怎么定义一个类的静态变量
- 算法题：增量元素之间的最大差值
- 你这次看机会的原因是什么
- 反问阶段我感觉之前问的差不多了，这次就没再问什么问题了

四面HR

- 现在是离职还是在职状态

- 介绍一下之前负责的工作

- 用户量怎么样

- 这个项目是从 0 到 1 开发的吗

- 这个业务有什么特点，对于客户端开发有什么挑战与困难

- 团队分工是怎样的

- 这个项目能做成现在这个样子，你自己的核心贡献有哪些

- 这个事情对你来说有什么收获吗

- 在 B 站的工作节奏是怎么样的

- 离职的原因是什么呢

- 你自己希望找一个什么样的环境或者什么阶段的业务

- 你对小红书有什么了解吗

- 未来两三年对于职业发展的想法

- 你觉得现在有什么限制了你或者你觉得你需要提升哪些部分

- 反问阶段

- - 问了一些作息、福利待遇之类的问题

![image-20231228143149544](https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20231228143149544.png)







麦当劳一面：（强总）
1activity启动模式
2service两种启动
3handle
4事件分发几个方法，onclick事件执行
5强软弱虚的区别与实际应用，handle的使用
6内存泄漏场景，如何解决，leakcanary原理

7sp的commit和apply区别

1集中集合的区别
2arraylist与linklist区别
3hashmap
4volatile关键字，三种特性
5并发编程问题（我回答的不好，没有继续问下去）

rn有什么性能问题
rn的dom过多问题优化
点餐屏交互相关内容

<img src="https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20240319151220441.png" alt="image-20240319151220441" style="zoom: 33%;" /><img src="https://raw.githubusercontent.com/AndroidJiang/img2/master/images2/image-20240319151250171.png" alt="image-20240319151250171" style="zoom:33%;" />

**bitmap占内存大小计算**

https://juejin.cn/post/7036714008644157447

> **内存 = `width * height * nTargetDensity/inDensity * nTargetDensity/inDensity * 一个像素所占的内存`。**
>
> inDensity越大时,缩放系数(nTargetDensity/inDensity)就越小,所需内存也就越小。所以不建议吧图片放在低dpi目录下，优先找高
>
> 一般来说，Android会**更倾向于缩小较大的原始图像，而非放大较小的原始图像**。在此前提下：
>
> - 假设最接近设备屏幕密度的目录选项为xhdpi，如果图片资源存在，则匹配成功；
>
> - 如果不存在，系统就会从更高密度的资源目录下查找，xhdpi > xxhdpi > xxxhdpi > hdpi > mdpi > ldpi；
>
> - 如果还不存在，系统就会从**像素密度无关的资源目录nodpi**下查找；
>
> - 如果还不存在，系统就会向更低密度的资源目录下查找，依次为hdpi、mdpi、ldpi。
>
> 
>
>   手机屏幕大小 1080 x 1920（inTarget = 420），加载 xhdpi （inDensity = 320）中的图片 1920 x 1080，scale = 420 / 320，
>   最总我们可以得知 他的占用内存 1418 * 2520 * 4

**Android的应用包体积大小并不会直接影响应用运行时的内存占用,但它们之间是存在一定关系的。**

> 具体来说:
>
> 1. 应用包体积越大,通常意味着应用中包含的代码、资源文件(图片、多媒体文件等)就越多。
> 2. 当应用安装并运行时,这些代码和资源文件都需要被加载到内存中。
> 3. 因此,包体积较大的应用在运行过程中通常需要占用更多的内存空间。
> 4. 但是,并不是应用包体积所占的全部空间都会被加载至内存。实际加载到内存中的只是被当前使用到的那部分代码和资源。

**签名，v1和v2**

> V1：应该是通过ZIP条目进行验证，这样APK 签署后可进行许多修改
>
> V2：验证压缩文件的所有字节，而不是单个 ZIP 条目，签名后无法再更改
>
> 
>
> 1）只勾选v1签名并不会影响什么，但是在7.0上不会使用更安全的验证方式
> 2）只勾选V2签名7.0以下会直接安装完显示未安装，7.0以上则使用了V2的方式验证
> 3）同时勾选V1和V2则所有机型都没问题   推荐

三星 王均友

1、kotlin 流 flow stateFlow sharedFlow
2、Jetpack组件，livedata，viewmodel。
 viewmodel生命周期管理，怎么保证屏幕旋转时不去销毁实例
 还用过别的jetpack组件吗
3、handler机制说一下
4、一个线程可以有几个handler，怎么保证message发送到对应的handler
5、handler采用的是什么样的设计模式  享元 单例 
6、handler线程切换时，对共享资源的保护  threadlocal
7、ThreadLocal的实现方式
8、其他方法保证线程安全，同步有哪些方法，
9、synchronized和lock什么区别
10、减少apk包体积，除了图片还有什么方式

万声音乐

1、Android事件分发机制
2、View的绘制流程
3、自定义view有哪些要注意的点
4、需要刷新的话，怎么做。如果布局也要刷新呢，viewGroup的子view也要变化
5、handler 消息机制，阻塞会卡主线程吗
6、handler发送延时消息怎么做的
7、子线程可以创建handler吗
8、项目中处理过oom，说一下怎么处理的
9、内存泄漏的原因，有哪些情况
10、项目中有哪些性能优化的点
11、appstartup
12、项目中线程安全怎么处理的，
13、ArrayList LinkedList区别，什么场景哪个更好，线程安全吗，如果说要保证线程的安全性怎么处理
14、HashMap 和 TreeMap什么区别
15、协程启动方式，async await()方式
16、livedata数据倒灌怎么处理
17、个人优势
