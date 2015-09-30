package com.sudalv.subway;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

public class MainActivity extends Activity {
    private SlidingMenu slidingMenu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        slidingMenu = new SlidingMenu(this);//创建对象
        slidingMenu.setMode(SlidingMenu.LEFT);//设定模式，SlidingMenu在左边
        slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);//配置slidingmenu偏移出来的尺寸
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);//全屏都可以拖拽触摸，打开slidingmenu
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);//附加到当前的Aty上去
        slidingMenu.setMenu(R.layout.slidingmenumain);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //重载
        switch(keyCode){
            case KeyEvent.KEYCODE_MENU:
                slidingMenu.toggle(true);
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
