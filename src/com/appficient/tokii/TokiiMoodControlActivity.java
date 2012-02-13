package com.appficient.tokii;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import com.appficient.tokii.model.positionView;




import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;


public class TokiiMoodControlActivity extends Activity {
	
	GestureDetector gdIndex;
	private float moodIndexX;
	private float moodIndexY;
	private float moodIndexWidth;
	private int moodIndexSize;
	private int pagesAmount;
	private LayoutInflater li;
	private Context ctx;
	private WeakHashMap<String,Integer> _whmIcons;
	private ImageView pBall;
	private int itemPosition;
	private boolean isDrawed;
	private float indexMarginLeft;
	
	//Constants
	private final int ICONS_PER_PAGE = 12;
	private final int PIXELS_PER_ITEM_INDEX = 30; //I can set it according to the number of pages as well, but for layout reasons it's better if each item has a fixed hand-set value.
    
	
	LinearLayout moodIndex;
	RelativeLayout rlContainer;
	ArrayList<LinearLayout> pages;
	ViewPager viewPager;
	ArrayList<Integer> ibIcons; 
	ArrayList<Integer> ibTvIcons;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		//Application Context
		ctx = this.getApplicationContext();
		//Init icons array and hashmap
		moodIconArrayCreator();
		ibResourcesCreator();
		tvResourcesCreator();
		//Pages in the control are calculated from the # of items in the hashmap
		pagesAmount =  (_whmIcons.size() / ICONS_PER_PAGE); //There are 12 icons per page
		pagesAmount = _whmIcons.size() % ICONS_PER_PAGE > 0 ? pagesAmount+1 : pagesAmount; //round it up!
		//Create Pages
		pages = moodPagesCreator();
		//Index Linear Layout
		moodIndex = (LinearLayout) findViewById(R.id.llIndex);
		//Create the gesture detector for index
		gdIndex =  new GestureDetector(this, new MoodIndexGestureListener());
		moodIndexSize = pagesAmount; //Amount of pages in the viewPager 
		//ViewPager where icons are laying
		viewPager = (ViewPager) findViewById(R.id.viewPager);
		MoodPageAdapter mpa = new MoodPageAdapter(this,pages);
		//Fill it up!
		viewPager.setAdapter(mpa);
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				//here i draw the position ball too, so the user has the index indicate properly
				drawPositionBall((float) (position * PIXELS_PER_ITEM_INDEX)+indexMarginLeft + (PIXELS_PER_ITEM_INDEX/2));
			}

			@Override
			public void onPageScrolled(int position, float arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
		
		moodIndex.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				 //coordinates of touch
		         moodIndexX = event.getX();
		         moodIndexY = event.getY();
		         //Display the requested Page
		         displayListItem();
	
		         return false;
			}	
		});

		//RelativeLayout holding indexes
		rlContainer = (RelativeLayout) findViewById(R.id.rlIndexContainer);
		//PositionBall
		pBall = (ImageView) findViewById(R.id.ivPositionBall);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event)
    {		        
        if (gdIndex.onTouchEvent(event))
        {
            return true;
        } else
        {
        	int eventaction = event.getAction(); 

            switch (eventaction ) { 

            case MotionEvent.ACTION_UP: 
            	//After the user lifts his finger from the index layout i must decide where to put the positionBall
            	float actionX = (float)(itemPosition * PIXELS_PER_ITEM_INDEX)+indexMarginLeft + (PIXELS_PER_ITEM_INDEX/2);
            	actionX = actionX < indexMarginLeft + (PIXELS_PER_ITEM_INDEX/2) ? indexMarginLeft + (PIXELS_PER_ITEM_INDEX/2) : actionX; 
		    	drawPositionBall(actionX);
                break; 
            }
            return false;
        }
    }
	
	public void displayListItem()
    {
        // The position is defined depending on where the user touched minus the margin on the left of the first item in the index layout
        itemPosition = (int) ((moodIndexX - indexMarginLeft - (PIXELS_PER_ITEM_INDEX/2)) / PIXELS_PER_ITEM_INDEX); //PIXELS_PER_ITEM_INDEX/2 its added in the equation because the dot must be in the middle
        itemPosition = itemPosition >= moodIndexSize ? moodIndexSize-1 : itemPosition; // The max is always the moodIndexSize value. I can't have more positions than pages! Starts from zero  
        //Set page of view pager
        ViewPager vp = (ViewPager) findViewById(R.id.viewPager);
        vp.setCurrentItem(itemPosition);
    }
		
	private ArrayList<LinearLayout> moodPagesCreator(){
		ArrayList<LinearLayout> pArray = new ArrayList<LinearLayout>();
		//Create a page with icons
		int p = 0;
		Iterator it = _whmIcons.entrySet().iterator();
		do{
			//Each page must show the rows with the mood icons
			LayoutInflater li = LayoutInflater.from(ctx);
			LinearLayout page = new LinearLayout(ctx);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
			page.setOrientation(LinearLayout.VERTICAL);			
			//For each page add 4 rows and 12 icons per page.
			for(int i=0;i<4;i++){
				if (it.hasNext()){
					//If there are no more items i shouldnt keep creating rows
					LinearLayout row = new LinearLayout(ctx);
					row = (LinearLayout) li.inflate(R.layout.rowmoodicons, null);
					for(int j=0;j<3;j++){
						if (it.hasNext()){
							Entry<String,Integer> e = (Entry<String,Integer>)it.next();
							row.findViewById(ibIcons.get(j)).setBackgroundResource(e!=null ? e.getValue() : 0);
							((ImageButton)row.findViewById(ibIcons.get(j))).setOnClickListener(new OnClickListener() {
								
								@Override
								public void onClick(View v) {
									// TODO: Do something here!!
									showDialog(1);
								}
							});
							((TextView)row.findViewById(ibTvIcons.get(j))).setText(e!=null ? e.getKey() : "");
						}
					}
					page.addView(row);
				}
			}
			pArray.add(page);
			p++;
		}while (p<pagesAmount);
		return pArray;	
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus)
    {
		//Once the activity is rendered i can get the width of the objects
        super.onWindowFocusChanged(hasFocus);
        moodIndexWidth = (float) (moodIndex.getWidth());   
       
        //Draw index
        if (!isDrawed){
        	BitmapDrawable bd = (BitmapDrawable) this.getResources().getDrawable(R.drawable.pos);
        	int itemWidth = bd.getBitmap().getWidth(); //The width is used to draw each item correctly  
            //Knowing the width of the screen and the amount of pages, and the pixels for each item in the index layout, i can calculate the space on each side of the mood index to center it
        	float indexMargin = moodIndexWidth - (PIXELS_PER_ITEM_INDEX*moodIndexSize); //The margin is used to center this view
        	indexMarginLeft = (float) (indexMargin*.5); //Half of the space left to use as margin 
        	//Draw index indicators just once
        	isDrawed = !isDrawed; 
            //Add a circle for each page in the index linear layout
     		int i = 0;
       		while (i<pagesAmount){
                //ImageView Layout for the position buttons
       			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT); //Set the size of the imageButton containing the position item
       			ImageView iv = new ImageView(this);
       			iv.setBackgroundResource(R.drawable.pos);
       			iv.setTag(i);
       			if (i==0){
       				//PIXELS_PER_ITEM_INDEX/2 its added in the equation because the dot must be in the middle
       				params.setMargins((int)indexMarginLeft+(PIXELS_PER_ITEM_INDEX/2),0,0,0); //The first one is set according to the space that must be let free to the left of the index layout	
       			}else{
       				params.setMargins((int)PIXELS_PER_ITEM_INDEX-itemWidth,0,0,0); //This items margins are set with reference to the first item
       			}
   				moodIndex.addView(iv,params);
       			i++;
      		} 
       	//Draw the positionBall over the first imageview button
    	drawPositionBall(indexMarginLeft + (PIXELS_PER_ITEM_INDEX/2));
        }
   }

	//JG: Define a Gesture detector to work with this linear layout index scroll
	private class MoodIndexGestureListener extends GestureDetector.SimpleOnGestureListener
	{
		 @Override
		 public boolean onScroll(MotionEvent e1, MotionEvent e2,float distanceX, float distanceY)
		 {
		     // we know already coordinates of first touch
		     // we know as well a scroll distance
		     moodIndexX = moodIndexX - distanceX;
		     moodIndexY = moodIndexY - distanceY;
		
		     // when the user scrolls within index we can show the correct page
		     if (moodIndexX >= 0 && moodIndexY >= 0)
		     {
		    	 drawPositionBall(e2.getX());
		 		// Display the page that belongs to the actual index position.
		    	displayListItem();
		     }
		     return super.onScroll(e1, e2, distanceX, distanceY);
		 }
	}
	
	private class MoodPageAdapter extends PagerAdapter {

		private ArrayList<LinearLayout> views;

		public MoodPageAdapter(Context context, ArrayList<LinearLayout> pagesArray) {
			views = pagesArray;
		}

		@Override
		public void destroyItem(View view, int arg1, Object object) {
			((ViewPager) view).removeView((LinearLayout) object);
		}

		@Override
		public void finishUpdate(View arg0) {

		}

		@Override
		public int getCount() {
			return views.size();
		}

		@Override
		public Object instantiateItem(View view, int position) {
			View page = views.get(position);
			((ViewPager) view).addView(page);
			
			return page;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {

		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {

		}

	}
	
	
	public void moodIconArrayCreator(){
		_whmIcons = new WeakHashMap<String, Integer>();
		_whmIcons.put("angelic", R.drawable.lmood_angelic);
		_whmIcons.put("angry", R.drawable.lmood_angry);
		_whmIcons.put("aroused", R.drawable.lmood_aroused);
		_whmIcons.put("birthday", R.drawable.lmood_birthday);
		_whmIcons.put("bitchy", R.drawable.lmood_bitchy);
		_whmIcons.put("bored", R.drawable.lmood_bored);
		_whmIcons.put("brokenhearted", R.drawable.lmood_brokenhearted);
		_whmIcons.put("chilly", R.drawable.lmood_chilly);
		_whmIcons.put("confident", R.drawable.lmood_confident);
		_whmIcons.put("confused", R.drawable.lmood_confused);
		_whmIcons.put("courageous", R.drawable.lmood_courageous);
		_whmIcons.put("cranky", R.drawable.lmood_cranky);
		_whmIcons.put("curious", R.drawable.lmood_curious);
		_whmIcons.put("drunk", R.drawable.lmood_drunk);
		_whmIcons.put("edgy", R.drawable.lmood_edgy);
		_whmIcons.put("embarrassed", R.drawable.lmood_embarassed);
		_whmIcons.put("energetic", R.drawable.lmood_energetic);
		_whmIcons.put("envious", R.drawable.lmood_envious);
		_whmIcons.put("euphoric", R.drawable.lmood_euphoric);
		_whmIcons.put("excited", R.drawable.lmood_excited);
		_whmIcons.put("frisky", R.drawable.lmood_frisky);
		_whmIcons.put("frosty", R.drawable.lmood_frosty);
		_whmIcons.put("frustrated", R.drawable.lmood_frustrated);
		_whmIcons.put("generous", R.drawable.lmood_generous);
		_whmIcons.put("gingerly", R.drawable.lmood_gingerly);
		_whmIcons.put("grateful", R.drawable.lmood_grateful);
		_whmIcons.put("happy", R.drawable.lmood_happy);
		_whmIcons.put("helpful", R.drawable.lmood_helpful);
		_whmIcons.put("heroic", R.drawable.lmood_heroic);
		_whmIcons.put("holy", R.drawable.lmood_holy);
		_whmIcons.put("horny", R.drawable.lmood_horny);
		_whmIcons.put("hunakkah", R.drawable.lmood_hunakkah);
		_whmIcons.put("inspired", R.drawable.lmood_inspired);
		_whmIcons.put("invincible", R.drawable.lmood_invincible);
		_whmIcons.put("jolly", R.drawable.lmood_jolly);
		_whmIcons.put("kinky", R.drawable.lmood_kinky);
		_whmIcons.put("kissable", R.drawable.lmood_kissable);
		_whmIcons.put("lovestruck", R.drawable.lmood_lovestruck);
		_whmIcons.put("lucky", R.drawable.lmood_lucky);
		_whmIcons.put("mischevious", R.drawable.lmood_mischevious);
		_whmIcons.put("missingyou", R.drawable.lmood_missingyou);
		_whmIcons.put("neglected", R.drawable.lmood_neglected);
		_whmIcons.put("nostalgic", R.drawable.lmood_nostalgic);
		_whmIcons.put("outlandish", R.drawable.lmood_outlandish);
		_whmIcons.put("passionate", R.drawable.lmood_passionate);
		_whmIcons.put("peaceful", R.drawable.lmood_peaceful);
		_whmIcons.put("pensive", R.drawable.lmood_pensive);
		_whmIcons.put("sad", R.drawable.lmood_sad);
		_whmIcons.put("scared", R.drawable.lmood_scared);
		_whmIcons.put("sexy", R.drawable.lmood_sexy);
		_whmIcons.put("sharp", R.drawable.lmood_sharp);
		_whmIcons.put("shocked", R.drawable.lmood_shocked);
		_whmIcons.put("sick", R.drawable.lmood_sick);
		_whmIcons.put("silly", R.drawable.lmood_silly);
		_whmIcons.put("stressed", R.drawable.lmood_stressed);
		_whmIcons.put("surprised", R.drawable.lmood_surprised);
		_whmIcons.put("thankful", R.drawable.lmood_thankful);
		_whmIcons.put("thoughtful", R.drawable.lmood_thoughtful);
		_whmIcons.put("tired", R.drawable.lmood_tired);
		_whmIcons.put("unique", R.drawable.lmood_unique);
		_whmIcons.put("wet", R.drawable.lmood_wet);
	}

	
	public void ibResourcesCreator(){
		ibIcons = new ArrayList<Integer>();
		ibIcons.add(R.id.bIcon01);
		ibIcons.add(R.id.bIcon02);
		ibIcons.add(R.id.bIcon03);
	}
	
	
	public void tvResourcesCreator(){
		ibTvIcons = new ArrayList<Integer>();
		ibTvIcons.add(R.id.tvIcon01);
		ibTvIcons.add(R.id.tvIcon02);
		ibTvIcons.add(R.id.tvIcon03);
	}
	
	 
	@Override
	public Dialog onCreateDialog(int _id) {
		switch (_id) {
	    case (1):
	        // do the work to define the game over Dialog
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder.setMessage("Test")
	    	       .setCancelable(false)
	    	       .setNegativeButton("OK", new DialogInterface.OnClickListener() {
	    	           public void onClick(DialogInterface dialog, int id) {
	    	                dialog.dismiss();
	    	           }
	    	       });
	    	return builder.create();
		}
		return null;
	}

	
	public void drawPositionBall(float eX){
	  	RelativeLayout.LayoutParams relParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		relParams.leftMargin = (int) (eX);  //Here i keep the last item position. It's used to set the position ball after the user stop touching the screen
		relParams.addRule(RelativeLayout.CENTER_VERTICAL);
	   	//Erase this iv previous appearance
	   	rlContainer.removeView(pBall);
	   	rlContainer.addView(pBall, relParams);
	}
}