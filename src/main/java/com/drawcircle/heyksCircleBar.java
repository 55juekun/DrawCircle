package com.drawcircle;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import java.text.DecimalFormat;
import java.text.Format;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class heyksCircleBar extends View {
    private int START_ANGLE;//圆环开始位置，默认为0度也即最上方，范围为[0,359]
    private int END_ANGLE;//圆环结束位置，默认为360度也即最上方，范围为[1,718]且END_ANGLE-START_ANGLE<360且END_ANGLE>START_ANGLE
    private int MIN_VAL = 0;//环的最小值，即圆环处于开始位置应该显示的数值
    private int MAX_VAL = 360;//环的最大值，即圆环处于结束位置应该显示的数值
    private int curAngle;//前景环当前停留的位置,START_ANGLE<=curAngle<=END_ANGLE
    private int bgFakeStrokeWidth;//环背景的线条宽度，未经过换算
    private int fgFakeStrokeWidth;//环前景的线条宽度，未经过换算
    private int titleSize;//标题字号，未经过换算
    private int descriptionSize;//说明字号，未经过换算
    private int bgColor;//环背景颜色，背景应该只有一种颜色，默认灰色
    private int titleColor;//中心文字颜色，默认黑色
    private int[] fgColors = new int[]{Color.WHITE, Color.BLACK};//圆环所拥有的渐变颜色
    private float curVal;//环当前应该显示的数值，显示在中心位置
    private float bgRealStrokeWidth;//环背景的线条宽度，经过换算后实际宽度
    private float fgRealStrokeWidth;//环前景的线条宽度，经过换算后实际宽度
    private RectF bgCircleRectF = new RectF();//环背景
    private Format format;//显示数值的一些格式，默认为整数
    private Paint titlePaint;//画中心的字符
    private Paint descriptionPaint;//画中心的字符
    private Paint bgPaint;//画背景圆环
    private Paint fgPaint;//画前景圆环
    private String title;//环当前应该显示的字符，显示在中心位置，默认应该是curVal的数值
    private String description;//显示在数值下方的说明文字，默认为空，黑色颜色
    private float titleY, descriptionY;//title和description的纵轴坐标
    private BarAnimation mAnim;//自定义动画效果

    private Context context;//传入contex

    public heyksCircleBar(Context context) {
        this(context, null);
    }

    public heyksCircleBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public heyksCircleBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(attrs, defStyleAttr, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public heyksCircleBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
        init(attrs, defStyleAttr, defStyleRes);
    }

    public void setMIN_VAL(int MIN_VAL) {
        this.MIN_VAL = MIN_VAL;
    }

    public void setMAX_VAL(int MAX_VAL) {
        this.MAX_VAL = MAX_VAL;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public void setFgColors(int[] fgColors) {
        this.fgColors = fgColors;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private void init(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.heyksCircleBar, defStyleAttr, defStyleRes);
        START_ANGLE = array.getInteger(R.styleable.heyksCircleBar_startAngle, 0);
        END_ANGLE = array.getInteger(R.styleable.heyksCircleBar_endAngle, 360);
        curAngle = array.getInteger(R.styleable.heyksCircleBar_currentAngle, END_ANGLE);
        title = array.getString(R.styleable.heyksCircleBar_title);
        if (title == null) {
            title = "default";
        }
        description = array.getString(R.styleable.heyksCircleBar_description);
        if (description == null) {
            description = "default";
        }
        bgColor = array.getColor(R.styleable.heyksCircleBar_backgroundColor, Color.GRAY);
        titleColor = array.getColor(R.styleable.heyksCircleBar_titleColor, Color.BLACK);
        fgFakeStrokeWidth = array.getInteger(R.styleable.heyksCircleBar_foregroundFakeStrokeWidth, 10);
        bgFakeStrokeWidth = array.getInteger(R.styleable.heyksCircleBar_backgroundFakeStrokeWidth, 30);
        titleSize = array.getInteger(R.styleable.heyksCircleBar_titleSize, 120);
        descriptionSize = array.getInteger(R.styleable.heyksCircleBar_descriptionSize, 60);
        array.recycle();

        fgPaint = new Paint();
        fgPaint.setColor(Color.GREEN);
        fgPaint.setStyle(Paint.Style.STROKE);// 空心
        fgPaint.setStrokeCap(Paint.Cap.ROUND);// 圆角画笔
        fgPaint.setAntiAlias(true);// 去锯齿

        bgPaint = new Paint();
        bgPaint.setColor(bgColor);
        bgPaint.setStyle(Paint.Style.STROKE);
        bgPaint.setStrokeCap(Paint.Cap.ROUND);
        bgPaint.setAntiAlias(true);

        titlePaint = new Paint();
        titlePaint.setAntiAlias(true);
        titlePaint.setColor(titleColor);

        descriptionPaint = new Paint();
        descriptionPaint.setAntiAlias(true);
        descriptionPaint.setColor(Color.BLACK);

        mAnim = new BarAnimation();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawArc(bgCircleRectF, START_ANGLE - 85, END_ANGLE - START_ANGLE, false, bgPaint);
        canvas.drawText(title, bgCircleRectF.centerX() - (titlePaint.measureText(title) / 2), titleY, titlePaint);
        canvas.drawText(description, bgCircleRectF.centerX() - (descriptionPaint.measureText(description) / 2), descriptionY,
                descriptionPaint);

//        设置渐变
        SweepGradient sweepGradient = new SweepGradient(bgCircleRectF.centerX(), bgCircleRectF.centerY(), fgColors, null);
        Matrix matrix = new Matrix();
        matrix.setRotate(START_ANGLE - 90, bgCircleRectF.centerX(), bgCircleRectF.centerY());
        sweepGradient.setLocalMatrix(matrix);
        fgPaint.setShader(sweepGradient);

//        圆弧也是从最顶端开始的，如果选择-90的话会超出一部分颜色，因为圆弧的开始结尾都是光滑的
        canvas.drawArc(bgCircleRectF, START_ANGLE - 85, curAngle - START_ANGLE, false, fgPaint);
    }

    //获取当前View的宽高
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        int min = Math.min(width, height);// 获取View最短边的长度
        setMeasuredDimension(min, min);// 强制改View为以最短边为长度的正方形
        bgRealStrokeWidth = getTextScale(bgFakeStrokeWidth, min);// 背景圆弧的宽度
        fgRealStrokeWidth = getTextScale(fgFakeStrokeWidth, min);// 前景圆弧的宽度
        float marginLen = bgRealStrokeWidth + fgRealStrokeWidth;
        bgCircleRectF.set(marginLen, marginLen, min - marginLen, min - marginLen);// 设置矩形
        titlePaint.setTextSize(getTextScale(titleSize, min));
        descriptionPaint.setTextSize(getTextScale(descriptionSize, min));
        titleY = getTextScale(300, min);
        descriptionY = getTextScale(400, min);
        bgPaint.setStrokeWidth(bgRealStrokeWidth);
        fgPaint.setStrokeWidth(fgRealStrokeWidth);
        bgPaint.setShadowLayer(getTextScale(10, min), 0, 0, Color.rgb(127, 127, 127));// 设置阴影
    }

    public float getTextScale(float n, float m) {
        return n / 500 * m;
    }

    /**
     * 更新当前title的当前数值和设置一圈动画时间
     */
    public void update(int curVal, int time) {
        if (curVal > MAX_VAL)
            curVal = MAX_VAL;
        this.curVal = curVal;
        mAnim.setDuration(time);
        // setAnimationTime(time);
        this.startAnimation(mAnim);
    }


    /**
     * 进度条动画
     */
    public class BarAnimation extends Animation {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            if (format == null) {
                format = new DecimalFormat("#.0");
            }
            title = format.format(MIN_VAL + interpolatedTime * (curVal - MIN_VAL));
            curAngle =
                    (int) (START_ANGLE + interpolatedTime * (curVal - MIN_VAL) / (MAX_VAL - MIN_VAL) * (END_ANGLE - START_ANGLE));
            requestLayout();
        }
    }
}
