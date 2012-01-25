package nig.lang;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class ClassUtils {

	/**
	 * 
	 * A class can be used to dynamically add a directory or a jar file into classpath and load the classes inside the
	 * jar or directory.
	 * 
	 * @see <a href="http://forums.sun.com/thread.jspa?threadID=300557&start=45&tstart=0">Discussion</a>
	 * 
	 */
	public static class CustomURLClassLoader extends URLClassLoader {

		public CustomURLClassLoader(URL[] urls, ClassLoader parent) {
			super(urls, parent);
		}

		public void addFile(File f) throws MalformedURLException {
			addURL(f.toURL());
		}

	}

	/**
	 * Retrieve the "public static main" method from a class.
	 * 
	 * @param <T>
	 * @param cls
	 * @return
	 * 
	 * @see <a
	 *      href="http://java.sun.com/j2se/1.4.2/docs/api/java/lang/reflect/Method.html#invoke%28java.lang.Object,%20java.lang.Object[]%29">Ref
	 *      1</a>
	 * @see <a href="http://stackoverflow.com/questions/287645/java-reflection-how-to-know-if-a-method-is-static">Ref
	 *      2</a>
	 * @see <a href="http://www.javaworld.com/javaworld/javaqa/1999-07/06-qa-invoke.html">Ref 3</a>
	 * 
	 *      <pre>
	 * @code 
	 *      Method mainMethod = getMainMethod(new CustomURLClassLoader(new URL[] new File(Exec.pluginLibDirectory().getAbsolutePath() + "/" + debabelerJar).toURL() },Transcode.class.getClassLoader()).loadClass(mainClass)); 
	 *      mainMethod.invoke(null, new Object[] { mainArgs });
	 * }
	 * </pre>
	 */
	public static <T> Method getMainMethod(Class<T> cls) {

		Method mm = null;
		for (Method m : cls.getMethods()) {
			if (Modifier.isStatic(m.getModifiers())) {
				if (m.getName().equals("main")) {
					mm = m;
				}
			}
		}
		return mm;

	}

}
