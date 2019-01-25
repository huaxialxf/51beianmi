
import javax.sql.DataSource;

import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.activerecord.generator.Generator;
import com.jfinal.plugin.activerecord.generator.MetaBuilder;
import com.jfinal.plugin.druid.DruidPlugin;

/**
 * GeneratorDemo
 */
public class GeneratorCodeApp {

	public static DataSource getDataSource() {
		PropKit.use("jdbc.properties");
		String url = PropKit.get("db.url");
		String username = PropKit.get("db.username");
		String password = PropKit.get("db.password");

		DruidPlugin druidPlugin = new DruidPlugin(url, username, password);
		druidPlugin.start();
		return druidPlugin.getDataSource();
	}

	public static void main(String[] args) {

		DataSource ds = getDataSource();
		// base model 所使用的包名
		String modelPackageName = "com.marksmile.icp.tools.db.model";
		String srcFilePath = "src/main/java/";

		String baseModelPackageName = modelPackageName + ".base";
		// base model 文件保存路径
		String baseModelOutputDir = srcFilePath + baseModelPackageName.replace('.', '/');

		// model 所使用的包名 (MappingKit 默认使用的包名)
		// model 文件保存路径 (MappingKit 与 DataDictionary 文件默认保存路径)
		String modelOutputDir = baseModelOutputDir + "/..";

		// 创建生成器
		Generator gernerator = new Generator(ds, baseModelPackageName, baseModelOutputDir, modelPackageName,
				modelOutputDir);
		// 设置数据库方言
		gernerator.setDialect(new MysqlDialect());
		// 添加不需要生成的表名
		gernerator.setMetaBuilder(new MyMetaBuilder(ds));
		// gernerator.addExcludedTable("adv");
		// 设置是否在 Model 中生成 dao 对象
		gernerator.setGenerateDaoInModel(true);
		// 设置是否生成字典文件
		gernerator.setGenerateDataDictionary(true);
		// 设置需要被移除的表名前缀用于生成modelName。例如表名 "osc_user"，移除前缀 "osc_"后生成的model名为
		// "User"而非 OscUser
		gernerator.setRemovedTableNamePrefixes("cha_icp_");
		// 生成
		gernerator.generate();
	}
}

class MyMetaBuilder extends MetaBuilder {

	public MyMetaBuilder(DataSource dataSource) {
		super(dataSource);
	}

	protected boolean isSkipTable(String tableName) {
		if ("cha_icp_beian_domain_info".equalsIgnoreCase(tableName)) {
			return false;
		}
		return true;
	}
}