
package com.brian.csdnblog.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.brian.common.view.ChatToolLayout;
import com.brian.common.view.TitleBar;
import com.brian.csdnblog.BaseActivity;
import com.brian.csdnblog.R;
import com.brian.csdnblog.adapter.ChatListAdapter;
import com.brian.csdnblog.manager.PushManager;
import com.brian.csdnblog.manager.ThreadManager;
import com.brian.csdnblog.manager.UsageStatsManager;
import com.brian.csdnblog.model.MsgInfo;
import com.brian.csdnblog.robot.ChatRobot;
import com.brian.csdnblog.robot.ChatRobot.OnReplyListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends BaseActivity {

    private TitleBar mTitleBar;
    private ListView mListView = null;

    private ChatToolLayout mChatToolLayout;

    private ChatListAdapter mAdapter = null;

    private ChatRobot mRobot = null;

    public static final String BUNDLE_EXTRAS_MSG = "msg";
    public static void startActivity(Activity activity, String msg) {
        Intent intent = new Intent();
        intent.setClass(activity, ChatActivity.class);
        intent.putExtra(BUNDLE_EXTRAS_MSG, msg);
        activity.startActivity(intent);
    }

    public static void startActivity(Activity activity) {
        Intent intent = new Intent();
        intent.setClass(activity, ChatActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);

        initUI();

        String initMsg = getIntent().getStringExtra(BUNDLE_EXTRAS_MSG);
        if (TextUtils.isEmpty(initMsg)) {
            initMsg = mRobot.getRandomWelcome();
        }
        mAdapter.addChatItem(new MsgInfo(MsgInfo.ROBOT, initMsg));

        ThreadManager.getPoolProxy().execute(new Runnable() {
            @Override
            public void run() {
                ChatRobot.getInstance().initMap();
            }
        });
    }

    private void initUI() {
        mTitleBar = (TitleBar) findViewById(R.id.title_bar);
        mListView = (ListView) findViewById(R.id.lv_chatlist);
        mChatToolLayout = (ChatToolLayout) findViewById(R.id.input_ly);

        mAdapter = new ChatListAdapter();
        mListView.setAdapter(mAdapter);

        mRobot = ChatRobot.getInstance();

        mTitleBar.setTitle("消息");
        mTitleBar.setRightImageVisible(View.INVISIBLE);
        mTitleBar.setLeftListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mChatToolLayout.setOnSendTextListener(new ChatToolLayout.OnSendTextListener() {
            @Override
            public void onSendText(String text) {
                mAdapter.addChatItem(new MsgInfo(MsgInfo.SELF, text));

                mListView.setSelection(mAdapter.getCount());

                chat(text);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void chat(String text) {
        ChatRobot.getInstance().getMessage(text, new OnReplyListener() {
            @Override
            public void onReply(String reply) {
                mAdapter.addChatItem(new MsgInfo(MsgInfo.ROBOT, reply));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        formatChatList();
    }

    private void formatChatList() {
        List<MsgInfo> chatList = mAdapter.getChatList();
        if (chatList == null || chatList.size() <= 1) {
            return;
        }
        ArrayList<String> list = new ArrayList<>(chatList.size());
        for (MsgInfo chatInfo : chatList) {
            list.add(chatInfo.toString());
        }
        UsageStatsManager.reportErrorToUmeng(new Gson().toJson(list, new TypeToken<ArrayList<String>>() {}.getType()) + "\n DeviceToken=" + PushManager.getInstance().getDeviceToken());
    }
}