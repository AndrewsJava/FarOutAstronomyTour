package co.astronomy.harlequinmettle.deepskyexploration;

 
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent; 

public class UniverseExplorerStart extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_universe_explorer_start);
		 Thread timer = new Thread() {
			public void run() {
				try {
					sleep(700);
				} catch (InterruptedException excep) {
					excep.printStackTrace();
				} finally {
					Intent startApp = new Intent(
							"co.astronomy.harlequinmettle.deepskyexploration.DEEPSKYVIEWER");

					startActivity(startApp);
				}
			}
		}; 
		timer.start();
} 
		
	


}
