package cn.join.android.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.text.DecimalFormat;

import cn.join.android.R;


/**
 * 仿iphone带进度的进度条，线程安全的View，可直接在线程中更新进度
 * @author xiaanming
 *
 */
public class RoundProgressBar2 extends View {
	/**
	 * 画笔对象的引用
	 */
	private Paint paint;

	private Paint pointPaint;

	/**
	 * 圆环的颜色
	 */
	private int roundColor;

	/**
	 * 圆环进度的颜色
	 */
	private int roundProgressColor;

	/**
	 * 中间进度百分比的字符串的颜色
	 */
	private int textColor;

	/**
	 * 中间进度百分比的字符串的字体
	 */
	private float textSize;

	/**
	 * 圆环的宽度
	 */
	private float roundWidth;

	/**
	 * 最大进度
	 */
	private int max;

	/**
	 * 当前进度
	 */
	private float progress;
	/**
	 * 要到达的最大进度
	 */
	private float maxProgress;
	/**
	 * 是否显示中间的进度
	 */
	private boolean textIsDisplayable;

	private CompleteCallback completeCallback;
	/**
	 * 进度的风格，实心或者空心
	 */
	private int style;

	boolean ifCallBack = false;

	public static final int STROKE = 0;
	public static final int FILL = 1;

	public RoundProgressBar2(Context context) {
		this(context, null);
	}

	public RoundProgressBar2(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RoundProgressBar2(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		paint = new Paint();
		pointPaint = new Paint();

		TypedArray mTypedArray = context.obtainStyledAttributes(attrs,
				R.styleable.RoundProgressBar);

		//获取自定义属性和默认值
		roundColor = mTypedArray.getColor(R.styleable.RoundProgressBar_roundColor, Color.RED);
		roundProgressColor = mTypedArray.getColor(R.styleable.RoundProgressBar_roundProgressColor, Color.GREEN);
		textColor = mTypedArray.getColor(R.styleable.RoundProgressBar_textColor, Color.GREEN);
		textSize = mTypedArray.getDimension(R.styleable.RoundProgressBar_textSize, 15);
		roundWidth = mTypedArray.getDimension(R.styleable.RoundProgressBar_roundWidth, 5);
		max = mTypedArray.getInteger(R.styleable.RoundProgressBar_max, 100);
		textIsDisplayable = mTypedArray.getBoolean(R.styleable.RoundProgressBar_textIsDisplayable, true);
		style = mTypedArray.getInt(R.styleable.RoundProgressBar_circle_style, 0);

		mTypedArray.recycle();
	}

	int mCenterViewWidth = 5;
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		/**
		 * 画最外层的大圆环
		 */
		int centre = getWidth()/2; //获取圆心的x坐标
		int threePer = getWidth()/3;
		int radius = (int) (centre - roundWidth/2); //圆环的半径
		paint.setColor(roundColor); //设置圆环的颜色
		paint.setStyle(Paint.Style.STROKE); //设置空心
		paint.setStrokeWidth(roundWidth); //设置圆环的宽度
		paint.setAntiAlias(true);  //消除锯齿 
		canvas.drawCircle(centre, centre, radius, paint); //画出圆环

		Log.e("log", centre + "");

		/**
		 * 画进度百分比
		 */
		paint.setStrokeWidth(0);
		paint.setColor(textColor);
		paint.setTextSize(textSize);
		paint.setTypeface(Typeface.DEFAULT_BOLD); //设置字体
		float percent = (float)(((float)progress / (float)max) * 100);  //中间的进度百分比，先转换成float在进行除法运算，不然都为0
		DecimalFormat decimalFormat = new DecimalFormat(".0");//构造方法的字符格式这里如果小数不足2位,会以0补足.
		String p=decimalFormat.format(percent);//format 返回的是字符串
		float percentResult = Float.parseFloat(p);
		float textWidth = paint.measureText(percentResult + "%");   //测量字体宽度，我们需要根据字体的宽度设置在圆环中间

		if(textIsDisplayable  && style == STROKE){
			canvas.drawText(percentResult + "%", centre - textWidth / 2, threePer + textSize/2, paint); //画出进度百分比
		}


		/**
		 * 画圆弧 ，画圆环的进度
		 */

		//设置进度是实心还是空心
		paint.setStrokeWidth(roundWidth); //设置圆环的宽度
		paint.setColor(roundProgressColor);  //设置进度的颜色
		RectF oval = new RectF(centre - radius, centre - radius, centre
				+ radius, centre + radius);  //用于定义的圆弧的形状和大小的界限

		pointPaint.setColor(Color.RED);                    //设置画笔颜色 
		pointPaint.setStrokeWidth((float) 20.0);         //线宽  

		switch (style) {
			case STROKE:{
				paint.setStyle(Paint.Style.STROKE);
				canvas.drawArc(oval, 0, 360 * progress / max, false, paint);  //根据进度画圆弧
//				
//				float radian = 360 * progress / maxSize;
//				float x = (float) (centre + Math.cos(radian)*radius);
//				float y = (float) (centre + Math.sin(radian)*radius);				 
//				canvas.drawPoint(x,y,pointPaint); 
				break;
			}
			case FILL:{
				paint.setStyle(Paint.Style.FILL_AND_STROKE);
				if(progress !=0){
					canvas.drawArc(oval, 0, 360 * progress / max, true, paint);  //根据进度画圆弧
				}
				break;
			}
		}
		if(progress>=maxProgress&&maxProgress>0&&!ifCallBack){
			completeCallback.doComplete();
			ifCallBack = true;
		}
	}

	public boolean isIfCallBack() {
		return ifCallBack;
	}

	public void setIfCallBack(boolean ifCallBack) {
		this.ifCallBack = ifCallBack;
	}

	//平方和计算  
	private float dist2(float dx, float dy){
		return dx * dx + dy * dy;
	}


	public synchronized int getMax() {
		return max;
	}

	/**
	 * 设置进度的最大值
	 * @param max
	 */
	public synchronized void setMax(int max) {
		if(max < 0){
			throw new IllegalArgumentException("maxSize not less than 0");
		}
		this.max = max;
	}

	/**
	 * 获取进度.需要同步
	 * @return
	 */
	public synchronized float getProgress() {
		return progress;
	}

	/**
	 * 设置进度，此为线程安全控件，由于考虑多线的问题，需要同步
	 * 刷新界面调用postInvalidate()能在非UI线程刷新
	 * @param progress
	 */
	public synchronized void setProgress(float progress) {
		if(progress < 0){
			throw new IllegalArgumentException("progress not less than 0");
		}
		if(progress > max){
			progress = max;
		}
		if(progress <= max){
			this.progress = progress;
			postInvalidate();
		}

	}


	public int getCricleColor() {
		return roundColor;
	}

	public void setCricleColor(int cricleColor) {
		this.roundColor = cricleColor;
	}

	public int getCricleProgressColor() {
		return roundProgressColor;
	}

	public void setCricleProgressColor(int cricleProgressColor) {
		this.roundProgressColor = cricleProgressColor;
	}

	public int getTextColor() {
		return textColor;
	}

	public void setTextColor(int textColor) {
		this.textColor = textColor;
	}

	public float getTextSize() {
		return textSize;
	}

	public void setTextSize(float textSize) {
		this.textSize = textSize;
	}

	public float getRoundWidth() {
		return roundWidth;
	}

	public void setRoundWidth(float roundWidth) {
		this.roundWidth = roundWidth;
	}

	public float getMaxProgress() {
		return maxProgress;
	}

	public void setMaxProgress(float maxProgress) {
		this.maxProgress = maxProgress;
	}

	public interface CompleteCallback{
		void doComplete();
	}

	public void setCompleteCallback(CompleteCallback completeCallback) {
		this.completeCallback = completeCallback;
	}


}
