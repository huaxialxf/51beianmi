import com.jfinal.plugin.ehcache.CacheKit;
import com.jfinal.plugin.ehcache.EhCachePlugin;

public class CacheTester {
	public static void main(String[] args) {
		
		
		EhCachePlugin plugin = new EhCachePlugin();
		plugin.start();
		CacheKit.put("ip", "127", 1);
		Thread t_put = new Thread() {
			public void run() {
				while (true) {
//					CacheKit.put("ip", "127", (Integer) CacheKit.get("ip", "127") + 1);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
			};
		};
		t_put.setName("t_put");
		t_put.start();

		Thread t_get = new Thread() {
			public void run() {
				int n=0;
				while (true) {

					try {
						Thread.sleep(6000);
					} catch (InterruptedException e) {
					}
					System.out.println(CacheKit.get("ip", "127") + "---"+(n++));
				}
			};
		};
		t_get.setName("t_get");
		t_get.start();
	}
}
