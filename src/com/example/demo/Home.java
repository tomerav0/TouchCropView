package com.example.demo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;

public class Home extends Activity implements OnClickListener {

	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
	
		//drawing = (SignatureViewModed) findViewById(R.id.myTest);
		findViewById(R.id.button1).setOnClickListener(this);
		findViewById(R.id.button2).setOnClickListener(this);
		
		
	}


	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
		if(arg0.getId() == R.id.button1)
		{
			Intent i = new  Intent(this, MainActivity.class);
			startActivity(i);
		}
		else
		{
			Intent i = new  Intent(this, buildImage.class);
			startActivity(i);
		}
			
		
		
	}
	

}
