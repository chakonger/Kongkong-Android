package cn.leanvision.normalkongkong.widget;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.BounceInterpolator;

import cn.leanvision.common.util.DensityUtil;
import cn.leanvision.common.util.LogUtil;
import cn.leanvision.normalkongkong.R;

/**
 * @author lvshicheng
 * @date 2015-4-15 下午4:55:16
 * @description 显示蓝牙绑定进度
 */
@SuppressLint("DrawAllocation")
public class BoundProgressView extends View {

    private static final int CIRCLE_ANIM = 0x1101;
    private static final int LINE_ANIM = 0x1102;
    /**
     * 长度绘制的基准系数
     */
    private static final int MAX_COUNT_COEF = 5;
    /**
     * 99DBFF 默认背景圈颜色 FFDE00 黄色
     */
    private String[] colors = new String[]
            {"#99DBFF", "#FFDE00", "#FFFFFF"};
    /**
     * 底部显示文字
     */
    private static final int[] TEXTS = new int[]
            {R.string._bp_phone, R.string._bp_switch, R.string._bp_cloud};

    /**
     * 绘制水平线宽
     */
    private float line_width;
    /**
     * 绘制圆的最大半径
     */
    private float circle_width;
    /**
     * 绘制外圈宽度
     */
    private float stroke_width;
    /**
     * 绘制区域高度
     */
    private float height;
    /**
     * 绘制区域宽度
     */
    private float width;
    private ValueAnimator ofFloat;
    /**
     * 当前执行到第几步
     */
    private int step = -1;
    /**
     * 绘制文本的垂直方向基准线
     */
    private float text_height;
    /**
     * 绘制直线计数
     */
    private int count = 0;
    private Paint paint;

    // private int currentTestStep = 0;

    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case LINE_ANIM:
                    if (step == 0 && count >= 3 * MAX_COUNT_COEF) {
                        return;
                    } else if (step == 1 && count >= 3 * MAX_COUNT_COEF) {
                        return;
                    } else if (step == 2 && count >= 2 * MAX_COUNT_COEF) {
                        return;
                    } else if (step > 2) {
                        return;
                    }
                    invalidate();
                    count++;
                    handler.sendEmptyMessageDelayed(LINE_ANIM, 1 * 1000);
                    break;
                case CIRCLE_ANIM:
                    // if (currentTestStep > 3)
                    // return;
                    // setStep(currentTestStep);
                    // currentTestStep++;
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * @param context
     * @param attrs
     */
    public BoundProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);

        int dip_10 = DensityUtil.dip2px(getContext(), 10);
        int dip_22 = DensityUtil.dip2px(getContext(), 22);
        int dip_2 = DensityUtil.dip2px(getContext(), 2);

        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attrs, R.styleable.BoundProgressView);
        line_width = obtainStyledAttributes.getDimension(R.styleable.BoundProgressView_hor_line_width, dip_10);
        circle_width = obtainStyledAttributes.getDimension(R.styleable.BoundProgressView_circle_radius, dip_10);
        height = obtainStyledAttributes.getDimension(R.styleable.BoundProgressView_pro_height, dip_22);
        stroke_width = obtainStyledAttributes.getDimension(R.styleable.BoundProgressView_stroke_width, dip_2);
        obtainStyledAttributes.recycle();

        text_height = DensityUtil.dip2px(getContext(), 16);
        paint = new Paint();
    }

    /**
     * 设置当前成功执行到第几步
     */
    public void setStep(int step) {
        LogUtil.log(getClass(), "step : " + step);
        if (step == this.step)
            return;
        count = 0;
        this.step = step;
        startAnimation();
        handler.removeMessages(LINE_ANIM);
        if (step < 2)
            handler.sendEmptyMessageDelayed(LINE_ANIM, 1 * 1000);
    }

    public int getStep() {
        return step;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        width = canvas.getWidth();

        float cy = height / 2f;
        float piece = (width - 3 * circle_width) / 2f;

        canvas.clipRect(0, 0, width, height + text_height * 1.5f);
        drawLine(canvas, paint, piece);

        drawCircle(canvas, paint, circle_width * 1.5f, cy, circle_width, true);
        drawCircle(canvas, paint, piece + circle_width, cy, circle_width, step >= 1);
        drawCircle(canvas, paint, piece * 2 + circle_width, cy, circle_width, step >= 2);
        //drawCircle(canvas, paint, width - circle_width * 1.5f, cy, circle_width, step >= 3);

        if (count == 0) {
            if (step == 0) {
                drawCircleAnim(canvas, paint, circle_width * 1.5f, cy, circle_width);
            } else if (step == 1) {
                drawCircleAnim(canvas, paint, piece + circle_width, cy, circle_width);
            } else if (step == 2) {
                drawCircleAnim(canvas, paint, piece * 2 + circle_width, cy, circle_width);
            }
            /* else if (step == 3) {
                drawCircleAnim(canvas, paint, width - circle_width * 1.5f, cy, circle_width);
            }*/
        }
        drawText(canvas, paint, piece);
    }

    private void drawLine(Canvas canvas, Paint paint, float piece) {
        paint.setColor(Color.parseColor(colors[0]));
        canvas.drawRect(circle_width * 1.5f, (height - line_width) / 2f, width - circle_width * 1.5f, (height + line_width) / 2f, paint);

        float right = circle_width * 2f + step * piece;
        if (step == 0) {
            right += (piece - circle_width) * count / (MAX_COUNT_COEF * 3f);
        } else if (step == 1) {
            right += (piece - circle_width) * count / (MAX_COUNT_COEF * 2f);
        } else if (step == 2) {
//            right += (piece - circle_width) * count / (MAX_COUNT_COEF * 2f);
            right = width - circle_width * 1.5f;
        } else if (step == 3) {
            right = width - circle_width * 1.5f;
        }

        if (right > width - circle_width * 1.5f)
            right = width - circle_width * 1.5f;

        /*else if (step == 3) {
            right = width - circle_width * 1.5f;
        }*/
        paint.setColor(Color.parseColor(colors[1]));

        RectF rect = new RectF(circle_width * 1.5f, (height - line_width) / 2f, right, (height + line_width) / 2f);
        canvas.drawRoundRect(rect, line_width / 2, line_width, paint);
    }

    private void drawCircle(Canvas canvas, Paint paint, float cx, float cy, float circleWidth, boolean isSucc) {
        paint.setAntiAlias(true);

        if (isSucc) {
            paint.setColor(Color.parseColor(colors[2]));
            canvas.drawCircle(cx, cy, circleWidth, paint);
            paint.setColor(Color.parseColor(colors[1]));
            canvas.drawCircle(cx, cy, circleWidth - stroke_width, paint);
        } else {
            paint.setColor(Color.parseColor(colors[0]));
            canvas.drawCircle(cx, cy, circleWidth, paint);
        }
    }

    private void drawCircleAnim(Canvas canvas, Paint paint, float cx, float cy, float circleWidth) {
        if (ofFloat != null && ofFloat.isRunning()) {
            float f = (Float) ofFloat.getAnimatedValue();
            float new_circle_width = f * circleWidth;
            drawCircle(canvas, paint, cx, cy, new_circle_width, true);
            invalidate();
        } else {
            drawCircle(canvas, paint, cx, cy, circleWidth, true);
        }
    }

    /**
     * @param canvas
     * @param piece
     */
    private void drawText(Canvas canvas, Paint paint, float piece) {
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.parseColor(colors[2]));
        paint.setTextSize(DensityUtil.dip2px(getContext(), 16));
        paint.setStrokeWidth(3);
        paint.setTextAlign(Paint.Align.CENTER);

        float x = 0;
        float y = text_height + height;
        for (int i = 0; i < TEXTS.length; i++) {
            if (i == 0) {
                x = circle_width * 1.5f;
            } else if (i == 1 || i == 2) {
                x = piece * i + circle_width;
            } else {
                x = width - circle_width * 1.5f;
            }
            canvas.drawText(getContext().getString(TEXTS[i]), x, y, paint);
        }
    }

    private void startAnimation() {
        ofFloat = ValueAnimator.ofFloat(1f, 1.3f, 1f);
        ofFloat.setDuration(600);
        BounceInterpolator bounceInterpolator = new BounceInterpolator();
        ofFloat.setInterpolator(bounceInterpolator);
        ofFloat.start();
        invalidate();
    }
}
