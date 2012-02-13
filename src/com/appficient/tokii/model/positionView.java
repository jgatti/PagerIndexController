package com.appficient.tokii.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

public class positionView {

	private Bitmap img; // the image of the ball
	 private int coordX = 0; // the x coordinate at the canvas
	 private int coordY = 0; // the y coordinate at the canvas
	 private boolean goRight = true;
	 private boolean goDown = true;
	 
		public positionView(Context context, int drawable) {
			BitmapFactory.Options opts = new BitmapFactory.Options();
	        opts.inJustDecodeBounds = true;
	        img = BitmapFactory.decodeResource(context.getResources(), drawable); 
		}
		
		public positionView(Context context, int drawable, Point point) {
			BitmapFactory.Options opts = new BitmapFactory.Options();
	        opts.inJustDecodeBounds = true;
	        img = BitmapFactory.decodeResource(context.getResources(), drawable); 
			coordX= point.x;
			coordY = point.y;
		}
		
		
		public void setX(int newValue) {
	        coordX = newValue;
	    }
		
		public int getX() {
			return coordX;
		}

		void setY(int newValue) {
	        coordY = newValue;
	   }
		
		public int getY() {
			return coordY;
		}
		
		
		public Bitmap getBitmap() {
			return img;
		}
		
		public void moveBall(int goX, int goY) {
			// check the borders, and set the direction if a border has reached
			if (coordX > 270){
				goRight = false;
			}
			if (coordX < 0){
				goRight = true;
			}
			if (coordY > 400){
				goDown = false;
			}
			if (coordY < 0){
				goDown = true;
			}
			// move the x and y 
			if (goRight){
				coordX += goX;
			}else
			{
				coordX -= goX;
			}
			if (goDown){
				coordY += goY;
			}else
			{
				coordY -= goY;
			}
			
		}

}
