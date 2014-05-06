package com.marvell.tv.epg.app;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.marvell.tv.epg.EventDescriptor;
import com.marvell.tv.epg.app.EPGStore.FancyChannel;
import com.marvell.tv.livetv.Channel;

public class EPGMain extends Activity {
    protected WebView webView;
    protected static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 0;
    private static final String TAG = "EPGMain";
    protected Uri imageUri;
    protected Handler mHandler = new Handler();
    protected BroadcastReceiver receiver = null;
    static final String ACTION_SCHEDUAL_EVENT = "android.intent.action.SCHEDUAL_EVENT";
    static final String KEY_EXTRA_SCHEDUAL_EVENT_TITLE = "schedual_event_title";
    static final String KEY_EXTRA_SCHEDUAL_TYPE = "schedual_event_type";
    static final String KEY_EXTRA_SCHEDUAL_START_TIME = "start_time";
    static final String KEY_EXTRA_SCHEDUAL_END_TIME = "end_time";
    static final String KEY_EXTRA_SCHEDUAL_CHANNEL = "channel";
    static final int SCHEDUAL_TYPE_REMIND = EPGStore.ScheduledEvents.SCHE_TYPE_REMIND;
    static final int SCHEDUAL_TYPE_RECORD = EPGStore.ScheduledEvents.SCHE_TYPE_RECORD;
    static final long SCHEDUAL_DELAY = 1000 * 10;
    static final String NULL_EVENT_TITLE = "NULL Event";
    private String currentEventTitle;
    private String currentEventChannel;
    private long currentEventStartTime;
    private long currentEventEndTime;
    private int currentScheduleType;
    private boolean backKeyPressedOnce = false;

    private static final String JAVASCRIPT_INTERFACE_MODULE = "EPGUtils";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // prepare database
        ContentResolver resolver = getContentResolver();

        EPGStore.emptyAllEvents(resolver);
        EPGStore.emptyAllChannels(resolver);
        EPGStore.emptyAllLogs(resolver);
        populateEPGChannelsAndEventsIntoProvider();
        setUpReceiver();

        setContentView(R.layout.epg_web_view);
        webView = (WebView) findViewById(R.id.webView);
        webView.addJavascriptInterface(new WebAppInterface(this), JAVASCRIPT_INTERFACE_MODULE);
        webView.loadUrl("file:///android_asset/www/epg.html");
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onConsoleMessage(String message, int lineNumber,
                    String sourceID) {
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                EPGMain.this.webView.loadUrl(url);
                return true;
            }
        });
        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                Log.d(TAG, "onKey() keycode: " + keyCode + " KeyEvent: " + event);
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    String keyLabel = null;
                    boolean programmableKey = false;
                    if (keyCode == KeyEvent.KEYCODE_PROG_BLUE) {
                        keyLabel = "blue";
                        programmableKey = true;
                    } else if (keyCode == KeyEvent.KEYCODE_PROG_GREEN) {
                        keyLabel = "green";
                        programmableKey = true;
                    } else if (keyCode == KeyEvent.KEYCODE_PROG_RED) {
                        keyLabel = "red";
                        programmableKey = true;
                    } else if (keyCode == KeyEvent.KEYCODE_PROG_YELLOW) {
                        keyLabel = "yellow";
                        programmableKey = true;
                    } else {
                        char keyChar = event.getDisplayLabel();
                        Log.d(TAG, "keyChar: " + keyChar);
                        if (!Character.isLetterOrDigit(keyChar)) {
                            return false;
                        }
                        keyLabel = String.valueOf(keyChar);
                    }
                    webView.loadUrl("javascript:onKeyUp('" + keyLabel + "')");
                    return programmableKey;
                }
                return false;
            }
        });

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(false);

        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setSupportZoom(false);
        webSettings.setDisplayZoomControls(false);

        Log.d("faywong", "default fontsize = " + webSettings.getDefaultFontSize());
        webSettings.setDefaultFontSize(16);
        Log.d("faywong", "default changed to = " + webSettings.getDefaultFontSize());
        //webSettings.setDefaultZoom(ZoomDensity.FAR);
        //webView.loadUrl("file:///android_asset/www/gtv-jquery-demo/index.html");

        // webView.loadUrl("http://www.google.com/tv/spotlight-gallery.html");
        // webView.loadUrl("http://live.pps.tv");
        // webView.loadUrl("http://jsfiddle.net/57cM6/134/show");

        //webView.setBackgroundColor(Color.rgb(24, 24, 24));
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.requestFocus();

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        teardownReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        backKeyPressedOnce = false;
    }

    private boolean handleBackPressed() {
        if (backKeyPressedOnce) {
            super.onBackPressed();
            return true;
        }
        this.backKeyPressedOnce = true;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                backKeyPressedOnce = false;
            }
        }, 2000);
        Toast.makeText(this, R.string.exit_press_back_twice_message, Toast.LENGTH_SHORT).show();
        return false;
    }

    private static String buildEventPromptString(Resources res, String title, String channelNum, long startTime, long endTime) {
        if (res == null) {
            return "";
        }
        String playRangle = null;
        String startTimeString = DateFormat.getTimeInstance().format(new Date(startTime * 1000));
        String endTimeString = DateFormat.getTimeInstance().format(new Date(endTime * 1000));
        playRangle = startTimeString + "-" + endTimeString;
        return String.format(res.getString(R.string.program_format), title, channelNum, playRangle);
    }

    private void setUpReceiver() {
        if (receiver != null) return;
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context c, Intent intent) {
                    Log.d(TAG, "onReceive() in, intent: " + intent);
                    if (intent == null) {
                        return;
                    }
                    int scheduleType = intent.getIntExtra(KEY_EXTRA_SCHEDUAL_TYPE, 0);
                    String title = intent.getStringExtra(KEY_EXTRA_SCHEDUAL_EVENT_TITLE);
                    String channelNum = intent.getStringExtra(KEY_EXTRA_SCHEDUAL_CHANNEL);
                    long startTime = intent.getLongExtra(KEY_EXTRA_SCHEDUAL_START_TIME, 0);
                    long endTime = intent.getLongExtra(KEY_EXTRA_SCHEDUAL_END_TIME, 0);
                    EPGMain.this.currentEventTitle = title;
                    EPGMain.this.currentEventChannel = channelNum;
                    EPGMain.this.currentEventStartTime = startTime;
                    EPGMain.this.currentEventEndTime = endTime;
                    EPGMain.this.currentScheduleType = scheduleType;

                    Resources res = EPGMain.this.getResources();
                    String prompt = buildEventPromptString(res, title, channelNum, startTime, endTime);
                    Log.d(TAG, "scheduleType: " + scheduleType);
                    String confirmString = "";
                    if (scheduleType == SCHEDUAL_TYPE_REMIND) {
                        confirmString = res.getString(R.string.confirm_playback) + " " + prompt;
                    } else if (scheduleType == SCHEDUAL_TYPE_RECORD) {
                        confirmString = res.getString(R.string.confirm_record) + " " + prompt;
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(EPGMain.this, AlertDialog.THEME_HOLO_DARK);
                    builder.setMessage(confirmString)
                        .setCancelable(false)
                        .setPositiveButton(res.getString(R.string.yes),
                                new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                String title = EPGMain.this.currentEventTitle;
                                String channelNum = EPGMain.this.currentEventChannel;
                                long startTime = EPGMain.this.currentEventStartTime;
                                long endTime = EPGMain.this.currentEventEndTime;
                                int scheduleType = EPGMain.this.currentScheduleType;

                                if (scheduleType == SCHEDUAL_TYPE_REMIND) {
                                    long now = System.currentTimeMillis() / 1000;
                                    if (now < startTime || now > endTime) {
                                        Resources res = EPGMain.this.getResources();
                                        String eventPrompt = buildEventPromptString(res, title, channelNum, startTime, endTime);
                                        eventPrompt =  String.format(res.getString(R.string.outdated_format), eventPrompt, res.getString(R.string.remind));
                                        Toast.makeText(EPGMain.this, eventPrompt,
                                                Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                        return;
                                    }
                                    _playBack(channelNum);
                                    removeScheduledProgram(title, channelNum, startTime, endTime, scheduleType);
                                    logWatchedProgram(title,
                                                      channelNum,
                                                      startTime,
                                                      endTime);
                                } else if (scheduleType == SCHEDUAL_TYPE_RECORD) {
                                    long now = System.currentTimeMillis() / 1000;
                                    if (now < startTime || now > endTime) {
                                        Resources res = EPGMain.this.getResources();
                                        String eventPrompt = buildEventPromptString(res, title, channelNum, startTime, endTime);
                                        eventPrompt =  String.format(res.getString(R.string.outdated_format), eventPrompt, res.getString(R.string.record));
                                        Toast.makeText(EPGMain.this, eventPrompt,
                                                Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                        return;
                                    }
                                    _record(channelNum);
                                    removeScheduledProgram(title, channelNum, startTime, endTime, scheduleType);
                                    logWatchedProgram(title,
                                                      channelNum,
                                                      startTime,
                                                      endTime);
                                }
                            }
                        })
                        .setNegativeButton(res.getString(R.string.no),
                                new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                     AlertDialog alert = builder.create();
                     alert.show();
                }
        };

        registerReceiver(receiver, new IntentFilter(ACTION_SCHEDUAL_EVENT));
    }

    private void teardownReceiver() {
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    private void logWatchedProgram(String title, String channelNum, long startTime, long endTime) {
        Log.d(TAG, "logWatchedProgram() in, title:" + title + " channelNum:" + channelNum);
        ContentResolver resolver = getContentResolver();
        EventDescriptor event = EPGStore.getEvent(
            resolver, title, channelNum, Long.toString(startTime), Long.toString(endTime));
        Log.d(TAG, "watched event:" + event);
        if (event != null) {
            EPGStore.insertWatchedEvent(resolver, event);
            String channelName = event.getChannel().toDisplayString();
            EPGStore.logWatchedChannel(resolver, channelName);
        } else {
            Log.e(TAG, "Event(title:" + title + ", channelNum:" + channelNum + ", startTime:"
                  + startTime + ", endTime:" + endTime + ") isn't existed!");
        }
    }

    private boolean insertScheduledProgram(String title, String channelNum, long startTime, long endTime, int scheType) {
        Log.d(TAG, "insertScheduledProgram() in, title: " + title);
        ContentResolver resolver = getContentResolver();
        EventDescriptor event = EPGStore.getEvent(
            resolver, title, channelNum, Long.toString(startTime), Long.toString(endTime));
        if (event == null) {
            return false;
        }

        return EPGStore.insertScheduledEvent(resolver, event.getChannel().toDisplayString(), event.getStartTime(), scheType);
    }

    private boolean removeScheduledProgram(String title, String channelNum, long startTime, long endTime, int scheType) {
        Log.d(TAG, "removeScheduledProgram() in, title:" + title + " channelNum:" + channelNum);
        ContentResolver resolver = getContentResolver();
        EventDescriptor event = EPGStore.getEvent(
            resolver, title, channelNum, Long.toString(startTime), Long.toString(endTime));
        if (event == null) {
            return false;
        }
        return EPGStore.removeScheduledEvent(resolver, event.getChannel().toDisplayString(), event.getStartTime(), scheType);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyUp(keyCode: " + keyCode + " event: " + event + ")");
        String specialKey = null;
        boolean stopPropagating = false;
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            specialKey = "back";
            webView.loadUrl("javascript:onKeyUp('" + specialKey + "')");
            return handleBackPressed();
        }
        return stopPropagating;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.web_view, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {

                // use imageUri here to access the image
                if (data != null) {
                    Bundle extras = data.getExtras();
                    Bitmap bmp = (Bitmap) extras.get("data");
                    Log.e("URI", "data is non-null");
                }
                String photo = imageUri.toString();
                String baseURL = photo.substring(0, photo.lastIndexOf("/"));
                String img = photo.substring(photo.lastIndexOf("/") + 1);
                Log.e("URI", "photo:" + photo);
                Log.e("URI", "baseURL:" + baseURL);
                Log.e("URI", "img:" + img);
                // String htmlString =
                // "<html><body><p>Hello Image!</p> <img src=\"file:///storage/sdcard/fname_1384330161897.jpg\" /></body></html>";
                // mWeb.loadDataWithBaseURL("", htmlString, "text/html",
                // "UTF-8", null);

                // here you will get the image as bitmap

            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Picture was not taken",
                        Toast.LENGTH_SHORT).show();
            }
        }

    }

    private ArrayList<EventDescriptor> getAllEPGEvents() {
        // TODO Auto-generated method stub
        return EPGStore.getEvents(getContentResolver(), null, null);
    }

    private ArrayList<EventDescriptor> getEPGEventsOfChannel(Channel channel) {
        // TODO Auto-generated method stub
        if (channel == null) {
            return null;
        }
        String selectionString = EPGStore.EventsColumns.CHANNEL_NUMBER + "=?";
        String[] selectionArgs = new String[] { channel.toDisplayString() };
        return EPGStore.getEvents(getContentResolver(), selectionString, selectionArgs);
    }

    /**
     *
     * @param originEvents
     *          the original events collection
     * @param startTime
     *          require events later than start time
     * @param duration
     *          require duration of events
     * @return
     */
    private ArrayList<EventDescriptor> filterEPGEvents(ArrayList<EventDescriptor> originEvents, long startTime, long duration) {
        if (originEvents == null || originEvents.size() == 0 || startTime <= 0) {
            Log.e(TAG, "Invalid parameter, originEvents:" + originEvents + " startTime:" + startTime);
            return null;
        }
        Log.d(TAG, "original events size:" + originEvents.size());
        long endTime = startTime + duration;
        ArrayList<EventDescriptor> eventsToRemove = new ArrayList<EventDescriptor>();

        for (EventDescriptor event : originEvents) {
            if ((event.getEndTime() <= startTime)
                    || (event.getStartTime() >= endTime)) {
                eventsToRemove.add(event);
            } else if (event.getStartTime() < startTime && event.getEndTime() > startTime) {
                event.setDuration(event.getEndTime() - startTime);
                event.setStartTime(startTime);
            } else if (event.getStartTime() < endTime && event.getEndTime() > endTime) {
                event.setDuration(endTime - event.getStartTime());
                event.setEndTime(endTime);
            }
        }

        for (EventDescriptor event : eventsToRemove) {
            Log.d(TAG, "remove event[startTime:" + event.getStartTime() + " title:" + event.getTitle() + "] in filterEPGEvents: by startTime: " + startTime + " duration:" + duration);
            originEvents.remove(event);
        }
        Log.d(TAG, "after filtering, original events size:" + originEvents.size());
        return originEvents;
    }

    private void dumpEvents(ArrayList<EventDescriptor> events) {
        if (events == null) {
            return;
        }
        Log.d(TAG, "##########################dump all events start##########################");

        for (EventDescriptor event: events) {
            Log.d(TAG, "event: " + event);
        }
        Log.d(TAG, "##########################dump all events end##########################");
    }

    private void populateEPGEventsForChannel(String channel) {
        if (channel == null) {
            return;
        }
        ContentResolver resolver = getContentResolver();
        long start = System.currentTimeMillis() / 1000;
        //Log.d(TAG, "start time in java:" + start);
        start -= 8200 ;
        EventDescriptor event = new EventDescriptor(new Channel(channel), "打狗棍  19", start, start + 2160, "打狗棍第19集，剧情...", "二丫头希望两个手下能提供让男性充满情欲的酒水，两个手下会过意来，立即找来了酒水给二丫头饮用。深夜，二丫头与素芝睡在床上，虽然喝了药酒，但他依然没有发现命根有动静，情急之下他从床上坐了起来，无可奈何看着素芝，素芝深明大理，劝说二丫头不要急燥。", "images/dgg.jpg", "com.marvell.willowtv.controller.device.TUNER", "电视剧", "Video", false, true);
        EPGStore.insertEvent(resolver, event);
        start += 2160;
        event = new EventDescriptor(new Channel(channel), "打狗棍  20", start, start + 2660, "打狗棍第20集，剧情...", "打狗棍第20集，详细剧情...", "images/dgg.jpg", "com.marvell.willowtv.controller.device.TUNER", "电视剧", "Video", false, true);
        start += 2660;
        EPGStore.insertEvent(resolver, event);

        event = new EventDescriptor(new Channel(channel), "爸爸去哪儿", start, start + 2800, "爸爸去哪儿，张亮让天天追森蝶...", "爸爸去哪儿，张亮让天天追森蝶，详细剧情...", "images/bbqne.jpg", "com.marvell.willowtv.controller.device.TUNER", "娱乐", "Video", false, true);
        start += 2800;
        EPGStore.insertEvent(resolver, event);

        event = new EventDescriptor(new Channel(channel), "天天向上", start, start + 2300, "天天向上，广告大伽...", "天天向上，广告大伽，详细剧情...", "images/ttxs.jpg", "com.marvell.willowtv.controller.device.TUNER", "娱乐", "Video", false, true);
        start += 2300;
        EPGStore.insertEvent(resolver, event);

        event = new EventDescriptor(new Channel(channel), "非常了得", start, start + 7200, "非常了得，郭德纲被美女锁喉...", "非常了得，郭德纲被美女锁喉，详细剧情...", "images/fcld.jpg", "com.marvell.willowtv.controller.device.TUNER", "娱乐", "Video", false, true);
        start += 7200;
        EPGStore.insertEvent(resolver, event);

        event = new EventDescriptor(new Channel(channel), "新闻联播", start, start + 3600, "习近平吃包子...", "新闻联播，习近平吃包子，详细剧情...", "images/xwlb.jpg", "com.marvell.willowtv.controller.device.TUNER", "新闻", "Video", false, true);
        start += 3600;
        EPGStore.insertEvent(resolver, event);

        // test for appendNullEvent function
        if (channel.equalsIgnoreCase("11")) {
            event = new EventDescriptor(new Channel(channel), "晚间新闻", start, start + 3600, "习近平不吃包子啦...", "晚间新闻，习近平不吃包子，改吃什么呢？详细剧情...", "images/xwlb.jpg", "com.marvell.willowtv.controller.device.TUNER", "新闻", "Video", false, true);
            start += 1800;
            EPGStore.insertEvent(resolver, event);
        }
    }

    private void populateEPGChannelsAndEventsIntoProvider() {
        Log.d(TAG, "populateEPGChannelsAndEventsIntoProvider() in");
        ContentResolver resolver = getContentResolver();
        FancyChannel channel = new FancyChannel("陕西卫视", "7", 7, "images/channel/陕西卫视65.jpg");
        EPGStore.insertChannel(resolver, channel);
        populateEPGEventsForChannel(channel.channelNum);

        channel = new FancyChannel("广东卫视", "8", 8, "images/channel/广东卫视65_50.jpg");
        EPGStore.insertChannel(resolver, channel);
        populateEPGEventsForChannel(channel.channelNum);

        channel = new FancyChannel("江苏靓妆", "9", 9, "images/channel/JSTV.gif");
        EPGStore.insertChannel(resolver, channel);
        populateEPGEventsForChannel(channel.channelNum);

        channel = new FancyChannel("河北卫视", "10", 10, "images/channel/HEBEI.gif");
        EPGStore.insertChannel(resolver, channel);
        populateEPGEventsForChannel(channel.channelNum);

        channel = new FancyChannel("江西卫视", "11", 11, "images/channel/江西卫视65.jpg");
        EPGStore.insertChannel(resolver, channel);
        populateEPGEventsForChannel(channel.channelNum);

        channel = new FancyChannel("东南卫视", "12", 12, "images/channel/FJTV.gif");
        EPGStore.insertChannel(resolver, channel);
        populateEPGEventsForChannel(channel.channelNum);

        channel = new FancyChannel("江西卫视", "13", 13, "images/channel/江西卫视65.jpg");
        EPGStore.insertChannel(resolver, channel);
        populateEPGEventsForChannel(channel.channelNum);

        channel = new FancyChannel("山东卫视", "14", 14, "images/channel/SDTV.gif");
        EPGStore.insertChannel(resolver, channel);
        populateEPGEventsForChannel(channel.channelNum);

        Log.d(TAG, "populateEPGChannelsAndEventsIntoProvider() out");
    }

    /**
     * feed EPG data from Java to JavaScript
     */
    public void feedEPG() {
        webView.loadUrl("javascript:feedEPG('" + buildEvents(0, 0) + "')");
    }

    private static class EventContainer {
        HashMap<String, ArrayList<EventDescriptor>> events;
        public EventContainer() {
            events = new HashMap<String, ArrayList<EventDescriptor>>();
        }

        public void add(String channelNum, ArrayList<EventDescriptor> eventsPerService) {
            events.put(channelNum, eventsPerService);
        }

        public String toJSON() {
            Log.d(TAG, "toJSON() step 1");
            if (events == null || events.size() == 0) {
                return "{}";
            }
            Log.d(TAG, "toJSON() step 2");
            StringBuilder sb1 = new StringBuilder();
            sb1.append("{");
            Iterator it = events.entrySet().iterator();
            Log.d(TAG, "toJSON() step 3, it:" + it);
            while (it.hasNext()) {
                Log.d(TAG, "toJSON() step 4");

                Map.Entry pairs = (Map.Entry)it.next();
                sb1.append("\"" + pairs.getKey() + "\":");

                ArrayList<EventDescriptor> eventsPerService = (ArrayList<EventDescriptor>)pairs.getValue();
                StringBuilder sb2 = new StringBuilder();
                sb2.append("[");
                int i = 0;
                for (i = 0; i < eventsPerService.size() - 1; i++) {
                    sb2.append(JSONFormatter.toJSON(eventsPerService.get(i)));
                    sb2.append(",");
                }
                sb2.append(JSONFormatter.toJSON(eventsPerService.get(i)));
                if (it.hasNext()) {
                    sb2.append("],");
                } else {
                    sb2.append("]");
                }

                sb1.append(sb2.toString());
            }
            sb1.append("}");
            Log.d(TAG, "toJSON() step 5");
            return sb1.toString();
        }
    }

    public static class JSONFormatter {
        public static String toJSON(EventDescriptor event) {
            StringBuilder sb = new StringBuilder();
            long now = System.currentTimeMillis() / 1000;
            // Log.d(TAG, "now: " + now);
            boolean isPlaying = (event.getStartTime() <= now && now <= event.getEndTime());
            String playRangle = null;
            String startTime = DateFormat.getTimeInstance().format(new Date(event.getStartTime() * 1000));
            String endTime = DateFormat.getTimeInstance().format(new Date(event.getEndTime() * 1000));
            playRangle = startTime + "-" + endTime;
            // For debugging
/*            if (event.getTitle().equalsIgnoreCase("天天向上")) {
                isPlaying = true;
            }*/
            sb.append("{");
            sb.append("\"title\":" + "\"" + event.getTitle() + "\",");
            sb.append("\"channel_num\":" + "\"" + event.getChannel().toDisplayString() + "\",");
            sb.append("\"start_time\":" + "" + event.getStartTime() + ",");
            sb.append("\"end_time\":" + "" + event.getEndTime() + ",");
            sb.append("\"playing\":" + (isPlaying ? 1 : 0) + ",");
            sb.append("\"play_range\":" + "\"" + playRangle + "\",");
            sb.append("\"duration\":" + event.getDuration()  + ",");
            sb.append("\"short_desc\":" + "\"" + event.getDescription() + "\",");
            sb.append("\"detail_desc\":" + "\"" + event.getLongDescription() + "\",");
            sb.append("\"pinyin_keyword\":" + "\"" + toPinyin(event.getTitle())  + "\",");
            sb.append("\"content_category\":" + "\"" + event.getContentType()  + "\",");
            sb.append("\"thumbnail_url\":" + "\"" + event.getThumbnailURL() + "\"");
            sb.append("}");
            return sb.toString();
        }

        public static String toJSON(ArrayList<FancyChannel> channels) {
            if (channels == null || channels.size() == 0) {
                return "{}";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            int i = 0;
            for (i = 0; i < channels.size() - 1; i++) {
                sb.append(JSONFormatter.toJSON(channels.get(i)));
                sb.append(",");
            }
            sb.append(JSONFormatter.toJSON(channels.get(i)));
            sb.append("]");
            return sb.toString();
        }

        public static String toJSON(FancyChannel channel) {
            if (channel == null) {
                return "{}";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"Id\":" + "\"" + channel.channelNum + "\",");
            sb.append("\"name\":" + "\"" + channel.displayName + "\",");
            sb.append("\"service_id\":" + "\"" + channel.serviceID + "\",");
            sb.append("\"logo_url\":" + "\"" + channel.resURL + "\",");
            sb.append("\"pinyin_keyword\":" + "\"" + toPinyin(channel.displayName)  + "\"");
            sb.append("}");
            return sb.toString();
        }

        public static String toJSON(String[] data) {
            if (data == null || data.length == 0) {
                return "{}";
            }

            StringBuilder sb = new StringBuilder();
            sb.append("[");

            int i = 0;
            for (i = 0; i < data.length - 1; i++) {
                sb.append("\"" + data[i] + "\"");
                sb.append(",");
            }

            sb.append("\"" + data[i] + "\"");
            sb.append("]");
            return sb.toString();
        }

        private static String toPinyin(final String hanziString) {
            StringBuffer sb = new StringBuffer();
            HanyuPinyinOutputFormat outPutFormat = new HanyuPinyinOutputFormat();
            outPutFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
            outPutFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
            char[] pinyinChars = hanziString.toCharArray();
            for (int i = 0; i < pinyinChars.length; i++) {
                char hanzi = pinyinChars[i];
                String[] pinyin = null;
                try {
                    pinyin = PinyinHelper.toHanyuPinyinStringArray(hanzi, outPutFormat);
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (pinyin != null && pinyin.length > 0) {
                    if (i == (pinyinChars.length - 1)) {
                        sb.append(pinyin[0]);
                    } else {
                        sb.append(pinyin[0] + " ");
                    }
                }
            }
            String pinyinString = sb.toString().trim();
            //Log.d(TAG, "Hanzi: " + hanziString + " Pinyin: " + pinyinString);
            return pinyinString;
        }
    }

    public String buildChannels() {
        ContentResolver resolver = getContentResolver();
        String jsonString = JSONFormatter.toJSON(EPGStore.getAllChannels(resolver));
        EPGStore.logToDB(resolver, "######################buildChannels() all EPG channels START #######################");
        EPGStore.logToDB(resolver, jsonString);
        EPGStore.logToDB(resolver, "######################buildChannels() all EPG channels END #######################");

        return jsonString;
    }

    private boolean appendNullEvent(ArrayList<EventDescriptor> events, long latestEventEndTime) {
        Log.d(TAG, "needAppendNullEvent() in");

        boolean result = true;
        if (events == null || events.size() == 0) {
            result = false;
        } else {
            EventDescriptor lastEvent = events.get(events.size() - 1);
            boolean needAppendNullEvent = (latestEventEndTime > lastEvent.getEndTime());
            Log.d(TAG, "needAppendNullEvent:" + needAppendNullEvent);
            if (needAppendNullEvent) {
                EventDescriptor newLastEvent = new EventDescriptor(new Channel(lastEvent.getChannel().toDisplayString()), "NULL Event", lastEvent.getEndTime(), latestEventEndTime, "NULL Event", "NULL Event", "images/xwlb.jpg", "com.marvell.willowtv.controller.device.TUNER", "NULL", "Video", false, true);
                events.add(newLastEvent);
            }
        }
        return result;
    }

    public String buildEvents(long startTime, long duration) {
        ContentResolver resolver = getContentResolver();
        EventContainer container = new EventContainer();
        Log.d(TAG, "check point 1");
        ArrayList<FancyChannel> channels = EPGStore.getAllChannels(resolver);
        Log.d(TAG, "check point 5");

        long largestEventEndTime = EPGStore.getLatestEventEndTime(resolver);

        Log.d(TAG, "check point 5-1, largestEndTime:" + largestEventEndTime);

        //dumpEvents(eventsPerService);
        for (FancyChannel channel : channels) {
            ArrayList<EventDescriptor> eventsPerChannel = getEPGEventsOfChannel(new Channel(channel.channelNum));
            // TODO: un-comment this for integration test
            //Log.d(TAG, "before filterEPGEvents eventsPerChannel.size:" + eventsPerChannel.size());
            //eventsPerChannel = filterEPGEvents(eventsPerChannel, startTime, duration);
            //Log.d(TAG, "after filterEPGEvents eventsPerChannel.size:" + eventsPerChannel.size());
            appendNullEvent(eventsPerChannel, largestEventEndTime);
            container.add(channel.channelNum, eventsPerChannel);
        }
        Log.d(TAG, "check point 6");

        EPGStore.logToDB(resolver, "######################buildEvents() all EPG events START #######################");
        Log.d(TAG, "check point 7");

        String jsonString = container.toJSON();
        EPGStore.logToDB(resolver, jsonString);
        EPGStore.logToDB(resolver, "######################buildEvents() all EPG events END #######################");
        Log.d(TAG, "check point 8, jsonString:" + jsonString);

        return jsonString;
    }

    private void scheduleEvent(int schedualType, String title, String channelNum, long startTime, long endTime) {
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        Intent schedualIntent = new Intent(ACTION_SCHEDUAL_EVENT);
        schedualIntent.putExtra(KEY_EXTRA_SCHEDUAL_TYPE, schedualType);
        schedualIntent.putExtra(KEY_EXTRA_SCHEDUAL_START_TIME, startTime);
        schedualIntent.putExtra(KEY_EXTRA_SCHEDUAL_END_TIME, endTime);
        schedualIntent.putExtra(KEY_EXTRA_SCHEDUAL_CHANNEL, channelNum);
        schedualIntent.putExtra(KEY_EXTRA_SCHEDUAL_EVENT_TITLE, title);

        PendingIntent pi = PendingIntent.getBroadcast(this, 0, schedualIntent, 0);
        am.set( AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() +
                SCHEDUAL_DELAY, pi);
        String schedualTypeString = null;
        if (schedualType == SCHEDUAL_TYPE_REMIND) {
            schedualTypeString = "Remind program";
        } else if (schedualType == SCHEDUAL_TYPE_RECORD) {
            schedualTypeString = "Record program";
        }

        String playRangle = null;
        String startTimeString = DateFormat.getTimeInstance().format(new Date(startTime * 1000));
        String endTimeString = DateFormat.getTimeInstance().format(new Date(endTime * 1000));
        playRangle = startTimeString + "-" + endTimeString;
        insertScheduledProgram(title, channelNum, startTime, endTime, schedualType);
        Toast.makeText(this, "Registerred " + schedualTypeString + " \"" + title + "\" @ channel " + channelNum + " [" + playRangle + "] :)", Toast.LENGTH_SHORT).show();
    }

    private void _playBack(String channelNum) {
        Log.d(TAG, "_playBack() in, channelNum: " + channelNum);
        Uri dataUri = Uri.parse("tv://passthrough?deviceId=com.marvell.willowtv.controller.device.TUNER");
        Intent playbackIntent = new Intent(Intent.ACTION_VIEW, dataUri);
        playbackIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        startActivity(playbackIntent);
    }

    private void _record(String channelNum) {
        Log.d(TAG, "_record() in, channelNum: " + channelNum);
        _playBack(channelNum);
    }


    public class WebAppInterface {
        Context mContext;
        WebAppInterface(Context c) {
            mContext = c;
        }

        /**
         * called by JavaScript code to retrieve the latest EPG data in JSON format
         *
         * @param startTime in UTC second(aka. Unix time)
         * @param duration in unit of second
         * @return EPG events of JSON format
         */
        @JavascriptInterface
        public String getEPG(long startTime, long duration) {
            Log.d(TAG, "getEPG() in, start time required in JavaScript:" + startTime + " duration:" + duration);
            String events = buildEvents(startTime, duration);
            Log.d(TAG, "getEPG returned: " + events);
            return events;
        }

        @JavascriptInterface
        public String getChannelList() {
            Log.d(TAG, "getChannelList() in");
            String channels = buildChannels();
            Log.d(TAG, "getChannelList returned: " + channels);
            return channels;
        }

        @JavascriptInterface
        public void playback(String channelNum) {
            _playBack(channelNum);
        }

        @JavascriptInterface
        public void record(String title, String channelNum, String startTime, String endTime) {
            Log.d(TAG, "record() in, channelNum: " + channelNum + " startTime: " + startTime + " endTime:" + endTime);
            EventDescriptor event = EPGStore.getEvent(getContentResolver(), title, channelNum, startTime, endTime);
            if (event != null) {
                String verifiedTitle = event.getTitle();
                Log.d(TAG, "record event: " + event);
                scheduleEvent(SCHEDUAL_TYPE_RECORD, verifiedTitle, channelNum, Long.parseLong(startTime), Long.parseLong(endTime));
            } else {
                Toast.makeText(EPGMain.this, "The event \"" + title + "\"" + " @ channel " + channelNum + " is not valid yet!", Toast.LENGTH_SHORT ).show();
            }
        }

        @JavascriptInterface
        public void remind( String title, String channelNum, String startTime, String endTime) {
            Log.d(TAG, "record() in, channelNum: " + channelNum + " startTime: " + startTime + " endTime:" + endTime);
            EventDescriptor event = EPGStore.getEvent(getContentResolver(), title, channelNum, startTime, endTime);
            if (event != null) {
                String verifiedTitle = event.getTitle();
                Log.d(TAG, "remind event: " + event);
                scheduleEvent(SCHEDUAL_TYPE_REMIND, verifiedTitle, channelNum, Long.parseLong(startTime), Long.parseLong(endTime));
            } else {
                Toast.makeText(EPGMain.this, "The event \"" + title + "\"" + " @ channel " + channelNum +  " is not valid yet!", Toast.LENGTH_SHORT ).show();
            }
        }

        @JavascriptInterface
        public String getContentTypeList() {
            Log.d(TAG, "getContentTypeList() in");
            String[] contentTypes = EPGStore.getContentTypes(getContentResolver());
            String jsonString = JSONFormatter.toJSON(contentTypes);
            Log.d(TAG, "jsonString: " + jsonString);
            return jsonString;
        }
    }
}