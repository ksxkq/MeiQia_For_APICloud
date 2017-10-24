package com.meiqia.meiqiasdk.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.meiqia.meiqiasdk.callback.OnEvaluateRobotAnswerCallback;
import com.meiqia.meiqiasdk.model.BaseMessage;
import com.meiqia.meiqiasdk.model.RobotMessage;
import com.meiqia.meiqiasdk.util.MQConfig;
import com.meiqia.meiqiasdk.util.MQResUtils;
import com.meiqia.meiqiasdk.util.MQUtils;

/**
 * OnePiece
 * Created by xukq on 6/24/16.
 */
public class MQWebViewActivity extends Activity implements View.OnClickListener {

    public static final String CONTENT = "content";

    private RelativeLayout mTitleRl;
    private RelativeLayout mBackRl;
    private TextView mBackTv;
    private ImageView mBackIv;
    private TextView mTitleTv;
    private WebView mWebView;

    private RelativeLayout mEvaluateRl;
    private TextView mUsefulTv;
    private TextView mUselessTv;
    private TextView mAlreadyFeedbackTv;

    public static RobotMessage sRobotMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(MQResUtils.getResLayoutID("mq_activity_webview"));

        findViews();
        setListeners();
        applyCustomUIConfig();
        logic();
    }

    private void findViews() {
        mTitleRl = (RelativeLayout) findViewById(MQResUtils.getResIdID("title_rl"));
        mBackRl = (RelativeLayout) findViewById(MQResUtils.getResIdID("back_rl"));
        mBackTv = (TextView) findViewById(MQResUtils.getResIdID("back_tv"));
        mBackIv = (ImageView) findViewById(MQResUtils.getResIdID("back_iv"));
        mTitleTv = (TextView) findViewById(MQResUtils.getResIdID("title_tv"));
        mWebView = (WebView) findViewById(MQResUtils.getResIdID("webview"));

        mEvaluateRl = (RelativeLayout) findViewById(MQResUtils.getResIdID("ll_robot_evaluate"));
        mUsefulTv = (TextView) findViewById(MQResUtils.getResIdID("tv_robot_useful"));
        mUselessTv = (TextView) findViewById(MQResUtils.getResIdID("tv_robot_useless"));
        mAlreadyFeedbackTv = (TextView) findViewById(MQResUtils.getResIdID("tv_robot_already_feedback"));
    }

    private void setListeners() {
        mBackRl.setOnClickListener(this);
        mUsefulTv.setOnClickListener(this);
        mUselessTv.setOnClickListener(this);
        mAlreadyFeedbackTv.setOnClickListener(this);
    }

    private void applyCustomUIConfig() {
        if (MQConfig.DEFAULT != MQConfig.ui.backArrowIconResId) {
            mBackIv.setImageResource(MQConfig.ui.backArrowIconResId);
        }

        // 处理标题栏背景色
        MQUtils.applyCustomUITintDrawable(mTitleRl,android.R.color.white, MQResUtils.getResColorID("mq_activity_title_bg"), MQConfig.ui.titleBackgroundResId);

        // 处理标题、返回、返回箭头颜色
        MQUtils.applyCustomUITextAndImageColor(MQResUtils.getResColorID("mq_activity_title_textColor"), MQConfig.ui.titleTextColorResId, mBackIv, mBackTv, mTitleTv);

        // 处理标题文本的对其方式
        MQUtils.applyCustomUITitleGravity(mBackTv, mTitleTv);
    }

    private void logic() {
        if (getIntent() != null) {
            handleRobotRichTextMessage();
            String data = getIntent().getStringExtra(CONTENT);
            mWebView.loadDataWithBaseURL(null, data, "text/html", "utf-8", null);

        }
    }

    private void handleRobotRichTextMessage() {
        if (sRobotMessage != null) {
            if (TextUtils.equals(RobotMessage.SUB_TYPE_EVALUATE, sRobotMessage.getSubType())
                    || BaseMessage.TYPE_CONTENT_RICH_TEXT.equals(sRobotMessage.getContentType())) {
                mEvaluateRl.setVisibility(View.VISIBLE);
                if (sRobotMessage.isAlreadyFeedback()) {
                    mUselessTv.setVisibility(View.GONE);
                    mUsefulTv.setVisibility(View.GONE);
                    mAlreadyFeedbackTv.setVisibility(View.VISIBLE);
                } else {
                    mUselessTv.setVisibility(View.VISIBLE);
                    mUsefulTv.setVisibility(View.VISIBLE);
                    mAlreadyFeedbackTv.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == MQResUtils.getResIdID("back_rl")) {
            onBackPressed();
        } else if (id == MQResUtils.getResIdID("tv_robot_useful")) {
            evaluate(RobotMessage.EVALUATE_USEFUL);
        } else if (id == MQResUtils.getResIdID("tv_robot_useless")) {
            evaluate(RobotMessage.EVALUATE_USELESS);
        } else if (id == MQResUtils.getResIdID("tv_robot_already_feedback")) {
            mEvaluateRl.setVisibility(View.GONE);
        }
    }

    private void evaluate(int useful) {
        MQConfig.getController(this).evaluateRobotAnswer(sRobotMessage.getId(), sRobotMessage.getQuestionId(), useful, new OnEvaluateRobotAnswerCallback() {
            @Override
            public void onFailure(int code, String message) {
                MQUtils.show(MQWebViewActivity.this, MQResUtils.getResStringID("mq_evaluate_failure"));
            }

            @Override
            public void onSuccess(String message) {
                sRobotMessage.setAlreadyFeedback(true);
                handleRobotRichTextMessage();
            }
        });
    }
}