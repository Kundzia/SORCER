/*
 * Copyright 2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sorcer.provider.boot;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

import com.sun.jini.config.ConfigUtil;
import com.sun.jini.start.NonActivatableServiceDescriptor;
import com.sun.jini.start.ServiceDescriptor;

/**
 * Holds static attributes used during the startup of services and provides
 * utilities to obtain {@link com.sun.jini.start.ServiceDescriptor} instances
 * for SORCER services
 */
public class SorcerDescriptorUtil {
	final static Logger logger = Logger.getLogger("sorcer.provider.boot");
	
	/**
	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
	 * {@link sorcer.tools.webster.Webster}.
	 * 
	 * @param policy
	 *            The security policy file to use
	 * @param roots
	 *            The roots webster should serve
	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for
	 *         webster using an anonymous port. The <tt>webster.jar</tt> file
	 *         will be loaded from <tt>iGrid.home/common/sorcer/webster.com</tt>
	 * 
	 * @throws IOException
	 *             If there are problems getting the anonymous port
	 * @throws RuntimeException
	 *             If the <tt>iGrid.home</tt> system property is not set
	 */
	public static ServiceDescriptor getWebster(String policy, String[] roots)
			throws IOException {
		return (getWebster(policy, 0, roots));
	}

	/**
	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
	 * {@link sorcer.tools.webster.Webster}.
	 * 
	 * @param policy
	 *            The security policy file to use
	 * @param port
	 *            The port webster should use
	 * @param roots
	 *            The roots webster should serve
	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for
	 *         webster using a specified port. The <tt>webster.jar</tt> file
	 *         will be loaded from <tt>iGrid.home/common/sorcer</tt>
	 * 
	 * @throws IOException
	 *             If there are problems getting the anonymous port
	 * @throws RuntimeException
	 *             If the <tt>iGrid.home</tt> system property is not set
	 */
	public static ServiceDescriptor getWebster(String policy, int port,
			String[] roots) throws IOException {
		return (getWebster(policy, port, roots, null, false, false));
	}
	public static ServiceDescriptor getWebster(String policy, int port,
			String[] roots, boolean isDaemon) throws IOException {
		return (getWebster(policy, port, roots, null, false, isDaemon));
	}
	
	/**
	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
	 * {@link sorcer.tools.webster.Webster}.
	 * 
	 * @param policy
	 *            The security policy file to use
	 * @param port
	 *            The port webster should use
	 * @param roots
	 *            The roots webster should serve
	 * @param debug
	 *            If true, set the <tt>sorcer.tools.debug</tt> property
	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for
	 *         webster using a specified port. The <tt>webster.jar</tt> file
	 *         will be loaded from <tt>${iGrid.home}/common/sorcer</tt>
	 * 
	 * @throws IOException
	 *             If there are problems getting the anonymous port
	 */
	public static ServiceDescriptor getWebster(String policy, int port,
			String[] roots, String address, boolean debug, boolean isDaemon) throws IOException {
		return (getWebster(policy, port, roots, address, 0, 0, debug, isDaemon));
	}
	
	/**
	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
	 * {@link sorcer.tools.webster.Webster}.
	 * 
	 * @param policy
	 *            The security policy file to use
	 * @param port
	 *            The port webster should use
	 * @param roots
	 *            The roots webster should serve
	 * @param debug
	 *            If true, set the <tt>sorcer.tools.debug</tt> property
	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for
	 *         webster using a specified port. The <tt>webster.jar</tt> file
	 *         will be loaded from <tt>${iGrid.home}/common/sorcer</tt>
	 * 
	 * @throws IOException
	 *             If there are problems getting the anonymous port
	 */
	public static ServiceDescriptor getWebster(String policy, int port,
			String[] roots, String address, int startPort, int endPort,
			boolean debug, boolean isDaemon) throws IOException {
		logger.info("policy: " + policy + ", port: " + port + ", roots: "
				+ Arrays.toString(roots) + ", address: " + "startPort: "
				+ startPort + ", endPort:" + endPort 
				+ ", debug: " + debug + ", isDaemon: " + isDaemon);
		int websterPort = 0;
		String iGridHome = getHomeDir();
		if (iGridHome == null)
			throw new RuntimeException("'iGrid.home' property not declared");
		String fs = File.separator;
		// anonymous case
		if (port == 0) {
			websterPort = Booter.getAnonymousPort(); 
			System.setProperty("webster.port", "" + port);
		}
		else if (port > 0) {
			websterPort = port;
		} else {
			// use SORCER environment configuration
			websterPort = Booter.getPort();
		}
		
		String webster = iGridHome + fs + "lib" + fs + "sorcer" + fs
				+ "lib-ext" + fs + "webster.jar";
		String websterRoots = concat(roots, ';');
		String websterClass = "sorcer.tools.webster.Webster";
		if (debug) {
			System.setProperty("sorcer.tools.webster.debug", "true");
		}
		String websterAddress = address;
		if (address == null || address.length() == 0) {
			websterAddress = Booter.getWebsterInterface();
			//Booter.getHostAddressFromProperty("java.rmi.server.hostname");
		}
		System.setProperty("webster.interface", websterAddress);
		
		return (new NonActivatableServiceDescriptor("", policy, webster,
				websterClass,
				new String[] { "-port", "" + websterPort, "-roots", websterRoots,
						"-bindAddress", websterAddress, "-startPort",
						"" + startPort, "-endPort", "" + endPort, 
						"-isDaemon", "" + isDaemon, "-debug", "" + debug}));
	}

	/**
	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
	 * {@link sorcer.core.provider.Spacer} using the Webster port created
	 * by this utility.
	 * 
	 * @param policy
	 *            The security policy file to use
	 * @param spacerConfig
	 *            The configuration options the Spacer will use
	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
	 *         Spacer using an anonymous port. The <tt>spacer.jar</tt> file
	 *         will be loaded from <tt>iGrid.home/lib</tt>
	 * 
	 * @throws IOException
	 *             If there are problems getting the anonymous port
	 * @throws RuntimeException
	 *             If the <tt>iGrid.home</tt> system property is not set
	 */
	public static ServiceDescriptor getSpacer(String policy,
			String... jobberConfig) throws IOException {
		return (getSpacer(policy, Booter.getPort(), jobberConfig));
	}

	/**
	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
	 * {@link sorcer.core.provider.Spacer}.
	 * 
	 * @param policy
	 *            The security policy file to use
	 * @param port
	 *            The port to use when constructing the codebase
	 * @param spacerConfig
	 *            The configuration options the Spacer will use
	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
	 *         Spacer using an anonymous port. The <tt>spacer.jar</tt> file
	 *         will be loaded from <tt>iGrid.home/lib</tt>
	 * 
	 * @throws IOException
	 *             If there are problems getting the anonymous port
	 * @throws RuntimeException
	 *             If the <tt>iGrid.home</tt> system property is not set
	 */
	public static ServiceDescriptor getSpacer(String policy, int port,
			String... jobberConfig) throws IOException {
		return (getSpacer(policy, Booter.getHostAddress(), port, jobberConfig));

	}

	/**
	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
	 * {@link sorcer.core.provider.Spacer}.
	 * 
	 * @param policy
	 *            The security policy file to use
	 * @param hostAddress
	 *            The address to use when constructing the codebase
	 * @param port
	 *            The port to use when constructing the codebase
	 * @param spacerConfig
	 *            The configuration options the Spacer will use
	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
	 *         Spacer using an anonymous port. The <tt>spacer.jar</tt> file
	 *         will be loaded from <tt>iGrid.home/lib</tt>
	 * 
	 * @throws IOException
	 *             If there are problems getting the anonymous port
	 * @throws RuntimeException
	 *             If the <tt>iGrid.home</tt> system property is not set
	 */
	public static ServiceDescriptor getSpacer(String policy,
			String hostAddress, int port, String... spacerConfig)
			throws IOException {
		String fs = File.separator;
		String iGridHome = System.getProperty("iGrid.home");
		if (iGridHome == null)
			throw new RuntimeException("'iGrid.home' property not declared");
		
		// service provider classpath
		String spacerClasspath = ConfigUtil.concat(new Object[] {
				iGridHome,fs,"lib",fs,"sorcer",fs,"lib",fs,"spacer.jar"});
		
		// service provider codebase
		String spacerCodebase = Booter.getCodebase(new String[] {
				"sorcer-prv-dl.jar", "spacer-dl.jar", "jsk-dl.jar", "serviceui.jar" },
				hostAddress, Integer.toString(port));
		String implClass = "sorcer.core.provider.jobber.ServiceSpacer";
		return (new SorcerServiceDescriptor(spacerCodebase, policy,
				spacerClasspath, implClass, spacerConfig));

	}

	/**
	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
	 * {@link sorcer.core.provider.Concatenator} using the Webster port created
	 * by this utility.
	 * 
	 * @param policy
	 *            The security policy file to use
	 * @param spacerConfig
	 *            The configuration options the Concatenator will use
	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
	 *         Spacer using an anonymous port. The <tt>concatenator.jar</tt> file
	 *         will be loaded from <tt>iGrid.home/lib</tt>
	 * 
	 * @throws IOException
	 *             If there are problems getting the anonymous port
	 * @throws RuntimeException
	 *             If the <tt>iGrid.home</tt> system property is not set
	 */
	public static ServiceDescriptor getConcatenator(String policy,
			String... concatenatorConfig) throws IOException {
		return (getConcatenator(policy, Booter.getPort(), concatenatorConfig));
	}

	/**
	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
	 * {@link sorcer.core.provider.Concarenator}.
	 * 
	 * @param policy
	 *            The security policy file to use
	 * @param port
	 *            The port to use when constructing the codebase
	 * @param spacerConfig
	 *            The configuration options the Concatenator will use
	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
	 *         Spacer using an anonymous port. The <tt>concatenator.jar</tt> file
	 *         will be loaded from <tt>iGrid.home/lib</tt>
	 * 
	 * @throws IOException
	 *             If there are problems getting the anonymous port
	 * @throws RuntimeException
	 *             If the <tt>iGrid.home</tt> system property is not set
	 */
	public static ServiceDescriptor getConcatenator(String policy, int port,
			String... concatenatorConfig) throws IOException {
		return (getConcatenator(policy, Booter.getHostAddress(), port, concatenatorConfig));

	}

	/**
	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
	 * {@link sorcer.core.provider.Concatenator}.
	 * 
	 * @param policy
	 *            The security policy file to use
	 * @param hostAddress
	 *            The address to use when constructing the codebase
	 * @param port
	 *            The port to use when constructing the codebase
	 * @param spacerConfig
	 *            The configuration options the Spacer will use
	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
	 *         Spacer using an anonymous port. The <tt>concatenator.jar</tt> file
	 *         will be loaded from <tt>iGrid.home/lib</tt>
	 * 
	 * @throws IOException
	 *             If there are problems getting the anonymous port
	 * @throws RuntimeException
	 *             If the <tt>iGrid.home</tt> system property is not set
	 */
	public static ServiceDescriptor getConcatenator(String policy,
			String hostAddress, int port, String... spacerConfig)
			throws IOException {
		String fs = File.separator;
		String iGridHome = System.getProperty("iGrid.home");
		if (iGridHome == null)
			throw new RuntimeException("'iGrid.home' property not declared");
		
		// service provider classpath
		String spacerClasspath = ConfigUtil.concat(new Object[] {
				iGridHome,fs,"lib",fs,"sorcer",fs,"lib",fs,"concatenator.jar"});
		
		// service provider codebase
		String spacerCodebase = Booter.getCodebase(new String[] {
				"sorcer-prv-dl.jar", "concatenator-dl.jar", "jsk-dl.jar", "serviceui.jar" },
				hostAddress, Integer.toString(port));
		String implClass = "sorcer.core.provider.jobber.ServiceConcatenator";
		return (new SorcerServiceDescriptor(spacerCodebase, policy,
				spacerClasspath, implClass, spacerConfig));

	}

	/**
	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
	 * {@link sorcer.core.provider.ServiceJobber} using the Webster port created
	 * by this utility.
	 * 
	 * @param policy
	 *            The security policy file to use
	 * @param monitorConfig
	 *            The configuration options the Monitor will use
	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
	 *         Monitor using an anonymous port. The <tt>monitor.jar</tt> file
	 *         will be loaded from <tt>iGrid.home/lib</tt>
	 * 
	 * @throws IOException
	 *             If there are problems getting the anonymous port
	 * @throws RuntimeException
	 *             If the <tt>iGrid.home</tt> system property is not set
	 */
	public static ServiceDescriptor getJobber(String policy,
			String... jobberConfig) throws IOException {
		return (getJobber(policy, Booter.getPort(), jobberConfig));
	}

	/**
	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
	 * {@link sorcer.core.provider.ServiceJobber}.
	 * 
	 * @param policy
	 *            The security policy file to use
	 * @param port
	 *            The port to use when constructing the codebase
	 * @param monitorConfig
	 *            The configuration options the Monitor will use
	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
	 *         Monitor using an anonymous port. The <tt>monitor.jar</tt> file
	 *         will be loaded from <tt>iGrid.home/lib</tt>
	 * 
	 * @throws IOException
	 *             If there are problems getting the anonymous port
	 * @throws RuntimeException
	 *             If the <tt>iGrid.home</tt> system property is not set
	 */
	public static ServiceDescriptor getJobber(String policy, int port,
			String... jobberConfig) throws IOException {
		return (getJobber(policy, Booter.getHostAddress(), port, jobberConfig));

	}

	/**
	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
	 * {@link sorcer.core.provider.ServiceJobber}.
	 * 
	 * @param policy
	 *            The security policy file to use
	 * @param hostAddress
	 *            The address to use when constructing the codebase
	 * @param port
	 *            The port to use when constructing the codebase
	 * @param monitorConfig
	 *            The configuration options the Monitor will use
	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
	 *         Monitor using an anonymous port. The <tt>monitor.jar</tt> file
	 *         will be loaded from <tt>iGrid.home/lib</tt>
	 * 
	 * @throws IOException
	 *             If there are problems getting the anonymous port
	 * @throws RuntimeException
	 *             If the <tt>iGrid.home</tt> system property is not set
	 */
	public static ServiceDescriptor getJobber(String policy,
			String hostAddress, int port, String... jobberConfig)
			throws IOException {
		String fs = File.separator;
		String iGridHome = System.getProperty("iGrid.home");
		if (iGridHome == null)
			throw new RuntimeException("'iGrid.home' property not declared");
		
		// service provider classpath
		String jobberClasspath = ConfigUtil.concat(new Object[] {
				iGridHome,fs,"lib",fs,"sorcer",fs,"lib",fs,"jobber.jar"
		});
		
		// service provider codebase
		String jobberCodebase = Booter.getCodebase(new String[] {
				"sorcer-prv-dl.jar", "jobber-dl.jar", "jsk-dl.jar", "serviceui.jar", "exertlet-ui.jar" },
				hostAddress, Integer.toString(port));
		String implClass = "sorcer.core.provider.jobber.ServiceJobber";
		return (new SorcerServiceDescriptor(jobberCodebase, policy,
				jobberClasspath, implClass, jobberConfig));

	}
	
	/**
	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
	 * {@link sorcer.core.provider.ServiceTasker} with beaned
	 * {@link sorcer.core.provider.exerter.ServiceExerter}, called Exerter.
	 * 
	 * @param policy
	 *            The security policy file to use
	 * @param exertConfig
	 *            The configuration options the ExertProcessor provider will use
	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
	 *         Exerter using an anonymous port. The <tt>sorcer-prv.jar</tt> file
	 *         will be loaded from <tt>iGrid.home/lib/sorcer/lib</tt>
	 * 
	 * @throws IOException
	 *             If there are problems getting the anonymous port
	 * @throws RuntimeException
	 *             If the <tt>iGrid.home</tt> system property is not set
	 */
	public static ServiceDescriptor getExerter(String policy,
			String... exerterConfig) throws IOException {
		return (getExerter(policy, Booter.getPort(), exerterConfig));
	}

	/**
	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
	 * {@link sorcer.core.provider.ServiceTasker} with beaned
	 * {@link sorcer.core.provider.exerter.ServiceExerter}, called Exerter.
	 * 
	 * @param policy
	 *            The security policy file to use
	 * @param port
	 *            The port to use when constructing the codebase
	 * @param exertConfig
	 *            The configuration options the ExertProcessor provider will use
	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
	 *         Exerter using an anonymous port. The <tt>sorcer-prv.jar</tt> file
	 *         will be loaded from <tt>iGrid.home/lib/sorcer/lib</tt>
	 * 
	 * @throws IOException
	 *             If there are problems getting the anonymous port
	 * @throws RuntimeException
	 *             If the <tt>iGrid.home</tt> system property is not set
	 */
	public static ServiceDescriptor getExerter(String policy, int port,
			String... exertConfig) throws IOException {
		return (getExerter(policy, Booter.getHostAddress(), port, exertConfig));

	}
	
	/**
	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
	 * {@link sorcer.core.provider.ServiceTasker} with beaned
	 * {@link sorcer.core.provider.exerter.ServiceExerter}, called Exerter.
	 * 
	 * @param policy
	 *            The security policy file to use
	 * @param hostAddress
	 *            The address to use when constructing the codebase
	 * @param port
	 *            The port to use when constructing the codebase
	 * @param exertConfig
	 *            The configuration options the ExertProcessor provider will use
	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
	 *         Exerter using an anonymous port. The <tt>sorcer-prv.jar</tt> file
	 *         will be loaded from <tt>iGrid.home/lib/sorcer/lib</tt>
	 * 
	 * @throws IOException
	 *             If there are problems getting the anonymous port
	 * @throws RuntimeException
	 *             If the <tt>iGrid.home</tt> system property is not set
	 */
	public static ServiceDescriptor getExerter(String policy,
			String hostAddress, int port, String... exerterConfig)
			throws IOException {
		String fs = File.separator;
		String iGridHome = System.getProperty("iGrid.home");
		if (iGridHome == null)
			throw new RuntimeException("'iGrid.home' property not declared");
		
		// service provider classpath
		String exerterClasspath = ConfigUtil.concat(new Object[] {
				iGridHome,fs,"lib",fs,"sorcer",fs,"lib",fs,"sorcer-prv.jar"
		});
		
		// service provider codebase
		String exerterCodebase = Booter.getCodebase(new String[] {
				"sorcer-prv-dl.jar", "jsk-dl.jar", "serviceui.jar", "exertlet-ui.jar" },
				hostAddress, Integer.toString(port));
		String implClass = "sorcer.core.provider.ServiceTasker";
		return (new SorcerServiceDescriptor(exerterCodebase, policy,
				exerterClasspath, implClass, exerterConfig));

	}

	/**
	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
	 * {@link sorcer.core.provider.exertmonitor.ExertMonitor} using the Webster port created
	 * by this utility.
	 * 
	 * @param policy
	 *            The security policy file to use
	 * @param exertmonitorConfig
	 *            The configuration options the ExertMonitor will use
	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
	 *         Monitor using an anonymous port. The <tt>exertmonitor.jar</tt> file
	 *         will be loaded from <tt>iGrid.home/lib</tt>
	 * 
	 * @throws IOException
	 *             If there are problems getting the anonymous port
	 * @throws RuntimeException
	 *             If the <tt>iGrid.home</tt> system property is not set
	 */
	public static ServiceDescriptor getExertMonitor(String policy,
			String... exertmonitorConfig) throws IOException {
		return (getExertMonitor(policy, Booter.getPort(), exertmonitorConfig));
	}

	/**
	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
	 * {@link sorcer.core.provider.exertmonitor.ExertMonitor}.
	 * 
	 * @param policy
	 *            The security policy file to use
	 * @param port
	 *            The port to use when constructing the codebase
	 * @param exertmonitorConfig
	 *            The configuration options the ExertMonitor will use
	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
	 *         Monitor using an anonymous port. The <tt>exertmonitor.jar</tt> file
	 *         will be loaded from <tt>iGrid.home/lib</tt>
	 * 
	 * @throws IOException
	 *             If there are problems getting the anonymous port
	 * @throws RuntimeException
	 *             If the <tt>iGrid.home</tt> system property is not set
	 */
	public static ServiceDescriptor getExertMonitor(String policy, int port,
			String... exertmonitorConfig) throws IOException {
		return (getExertMonitor(policy, Booter.getHostAddress(), port, exertmonitorConfig));

	}

	/**
	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
	 * {@link sorcer.core.provider.exertmonitor.ExertMonitor}.
	 * 
	 * @param policy
	 *            The security policy file to use
	 * @param hostAddress
	 *            The address to use when constructing the codebase
	 * @param port
	 *            The port to use when constructing the codebase
	 * @param exertmonitorConfig
	 *            The configuration options the ExertMonitor will use
	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
	 *         Monitor using an anonymous port. The <tt>exertmonitor.jar</tt> file
	 *         will be loaded from <tt>iGrid.home/lib</tt>
	 * 
	 * @throws IOException
	 *             If there are problems getting the anonymous port
	 * @throws RuntimeException
	 *             If the <tt>iGrid.home</tt> system property is not set
	 */
	public static ServiceDescriptor getExertMonitor(String policy,
			String hostAddress, int port, String... exertmonitorConfig)
			throws IOException {
		String fs = File.separator;
		String ps = File.pathSeparator;
		String iGridHome = System.getProperty("iGrid.home");
		if (iGridHome == null)
			throw new RuntimeException("'iGrid.home' property not declared");
		
		// service provider classpath
		String exertmonitor = ConfigUtil.concat(new Object[] {
				iGridHome,fs,"lib",fs,"sorcer",fs,"lib",fs,"exertmonitor.jar",
				ps,iGridHome,fs,"lib",fs,"common",fs,"je-4.1.21.jar"
		});
		
		// service provider codebase
		String jobberCodebase = Booter.getCodebase(new String[] {
				"sorcer-prv-dl.jar", "exertmonitor-dl.jar", "jsk-dl.jar", "outrigger-dl.jar", "serviceui.jar", "exertlet-ui.jar" },
				hostAddress, Integer.toString(port));
		String implClass = "sorcer.core.provider.exertmonitor.ExertMonitor";
		return (new SorcerServiceDescriptor(jobberCodebase, policy,
				exertmonitor, implClass, exertmonitorConfig));

	}

	/**
	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
	 * {@link sorcer.core.provider.DatabaseStorer} using the Webster port created
	 * by this utility.
	 * 
	 * @param policy
	 *            The security policy file to use
	 * @param exertmonitorConfig
	 *            The configuration options the DatabaseStore will use
	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
	 *         Monitor using an anonymous port. The <tt>sdb-prv.jar</tt> file
	 *         will be loaded from <tt>iGrid.home/lib</tt>
	 * 
	 * @throws IOException
	 *             If there are problems getting the anonymous port
	 * @throws RuntimeException
	 *             If the <tt>iGrid.home</tt> system property is not set
	 */
	public static ServiceDescriptor getDatabaseStorer(String policy,
			String... sdbConfig) throws IOException {
		return (getDatabaseStorer(policy, Booter.getPort(), sdbConfig));
	}

	/**
	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
	 * {@link sorcer.core.provider.DatabaseStorer}.
	 * 
	 * @param policy
	 *            The security policy file to use
	 * @param port
	 *            The port to use when constructing the codebase
	 * @param exertmonitorConfig
	 *            The configuration options the DatabaseStore will use
	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
	 *         Monitor using an anonymous port. The <tt>sdb-prv.jar</tt> file
	 *         will be loaded from <tt>iGrid.home/lib</tt>
	 * 
	 * @throws IOException
	 *             If there are problems getting the anonymous port
	 * @throws RuntimeException
	 *             If the <tt>iGrid.home</tt> system property is not set
	 */
	public static ServiceDescriptor getDatabaseStorer(String policy, int port,
			String... sdbConfig) throws IOException {
		return (getDatabaseStorer(policy, Booter.getHostAddress(), port, sdbConfig));

	}

	/**
	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
	 * {@link sorcer.core.provider.DatabaseStorer}.
	 * 
	 * @param policy
	 *            The security policy file to use
	 * @param hostAddress
	 *            The address to use when constructing the codebase
	 * @param port
	 *            The port to use when constructing the codebase
	 * @param sdbConfig
	 *            The configuration options the DatabaseStore will use
	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
	 *         Monitor using an anonymous port. The <tt>sdb-prv.jar</tt> file
	 *         will be loaded from <tt>iGrid.home/lib</tt>
	 * 
	 * @throws IOException
	 *             If there are problems getting the anonymous port
	 * @throws RuntimeException
	 *             If the <tt>iGrid.home</tt> system property is not set
	 */
	public static ServiceDescriptor getDatabaseStorer(String policy,
			String hostAddress, int port, String... sdbConfig)
			throws IOException {
		String fs = File.separator;
		String ps = File.pathSeparator;
		String iGridHome = System.getProperty("iGrid.home");
		if (iGridHome == null)
			throw new RuntimeException("'iGrid.home' property not declared");
		
		// service provider classpath
		String dbpc = ConfigUtil.concat(new Object[] {
				iGridHome,fs,"lib",fs,"sorcer",fs,"lib",fs,"dbp-prv.jar",
				ps,iGridHome,fs,"lib",fs,"common",fs,"je-4.1.21.jar"
		});
		
		// service provider codebase
		String dbpCodebase = Booter.getCodebase(new String[] {
				"sorcer-prv-dl.jar", "dbp-prv-dl.jar", "jsk-dl.jar", "serviceui.jar", "exertlet-ui.jar" },
				hostAddress, Integer.toString(port));
		String implClass = "sorcer.core.provider.dbp.DatabaseProvider";
		return (new SorcerServiceDescriptor(dbpCodebase, policy,
				dbpc, implClass, sdbConfig));

	}

	/**
	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
	 * {@link sorcer.core.provider.DataspaceStorer} using the Webster port created
	 * by this utility.
	 * 
	 * @param policy
	 *            The security policy file to use
	 * @param exertmonitorConfig
	 *            The configuration options the DataspaceStore will use
	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
	 *         Monitor using an anonymous port. The <tt>sdb-prv.jar</tt> file
	 *         will be loaded from <tt>iGrid.home/lib</tt>
	 * 
	 * @throws IOException
	 *             If there are problems getting the anonymous port
	 * @throws RuntimeException
	 *             If the <tt>iGrid.home</tt> system property is not set
	 */
	public static ServiceDescriptor getDataspaceStorer(String policy,
			String... sdbConfig) throws IOException {
		return (getDataspaceStorer(policy, Booter.getPort(), sdbConfig));
	}

	/**
	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
	 * {@link sorcer.core.provider.DataspaceStorer}.
	 * 
	 * @param policy
	 *            The security policy file to use
	 * @param port
	 *            The port to use when constructing the codebase
	 * @param exertmonitorConfig
	 *            The configuration options the DataspaceStore will use
	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
	 *         Monitor using an anonymous port. The <tt>sdb-prv.jar</tt> file
	 *         will be loaded from <tt>iGrid.home/lib</tt>
	 * 
	 * @throws IOException
	 *             If there are problems getting the anonymous port
	 * @throws RuntimeException
	 *             If the <tt>iGrid.home</tt> system property is not set
	 */
	public static ServiceDescriptor getDataspaceStorer(String policy, int port,
			String... sdbConfig) throws IOException {
		return (getDataspaceStorer(policy, Booter.getHostAddress(), port, sdbConfig));

	}

	/**
	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
	 * {@link sorcer.core.provider.DataspaceStorer}.
	 * 
	 * @param policy
	 *            The security policy file to use
	 * @param hostAddress
	 *            The address to use when constructing the codebase
	 * @param port
	 *            The port to use when constructing the codebase
	 * @param sdbConfig
	 *            The configuration options the DataspaceStore will use
	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
	 *         Monitor using an anonymous port. The <tt>sdb-prv.jar</tt> file
	 *         will be loaded from <tt>iGrid.home/lib</tt>
	 * 
	 * @throws IOException
	 *             If there are problems getting the anonymous port
	 * @throws RuntimeException
	 *             If the <tt>iGrid.home</tt> system property is not set
	 */
	public static ServiceDescriptor getDataspaceStorer(String policy,
			String hostAddress, int port, String... sdbConfig)
			throws IOException {
		String fs = File.separator;
		String ps = File.pathSeparator;
		String iGridHome = System.getProperty("iGrid.home");
		if (iGridHome == null)
			throw new RuntimeException("'iGrid.home' property not declared");
		
		// service provider classpath
		String dbpc = ConfigUtil.concat(new Object[] {
				iGridHome,fs,"lib",fs,"sorcer",fs,"lib",fs,"dsp-prv.jar",
				ps,iGridHome,fs,"lib",fs,"common",fs,"je-4.1.21.jar"
		});
		
		// service provider codebase
		String dbpCodebase = Booter.getCodebase(new String[] {
				"sorcer-prv-dl.jar", "dsp-prv-dl.jar", "jsk-dl.jar", "serviceui.jar", "exertlet-ui.jar" },
				hostAddress, Integer.toString(port));
		String implClass = "sorcer.core.provider.dbp.DataspaceProvider";
		return (new SorcerServiceDescriptor(dbpCodebase, policy,
				dbpc, implClass, sdbConfig));

	}

	/**
	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
	 * {@link sorcer.core.provider.Cataloger} using the Webster port created
	 * by this utility.
	 * 
	 * @param policy
	 *            The security policy file to use
	 * @param catalogerConfig
	 *            The configuration options the Cataloger will use
	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
	 *         Cataloger using an anonymous port. The <tt>cataloger.jar</tt> file
	 *         will be loaded from <tt>iGrid.home/lib</tt>
	 * 
	 * @throws IOException
	 *             If there are problems getting the anonymous port
	 * @throws RuntimeException
	 *             If the <tt>iGrid.home</tt> system property is not set
	 */
	public static ServiceDescriptor getCataloger(String policy,
			String... catalogerConfig) throws IOException {
		return (getCataloger(policy, Booter.getPort(), catalogerConfig));
	}

	/**
	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
	 * {@link sorcer.core.provider.Cataloger}.
	 * 
	 * @param policy
	 *            The security policy file to use
	 * @param port
	 *            The port to use when constructing the codebase
	 * @param catalogerConfig
	 *            The configuration options the Cataloger will use
	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
	 *         Cataloger using an anonymous port. The <tt>cataloger.jar</tt> file
	 *         will be loaded from <tt>iGrid.home/lib</tt>
	 * 
	 * @throws IOException
	 *             If there are problems getting the anonymous port
	 * @throws RuntimeException
	 *             If the <tt>iGrid.home</tt> system property is not set
	 */
	public static ServiceDescriptor getCataloger(String policy, int port,
			String... catalogerConfig) throws IOException {
		return (getCataloger(policy, Booter.getHostAddress(), port, catalogerConfig));

	}

	/**
	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
	 * {@link sorcer.core.provider.Cataloger}.
	 * 
	 * @param policy
	 *            The security policy file to use
	 * @param hostAddress
	 *            The address to use when constructing the codebase
	 * @param port
	 *            The port to use when constructing the codebase
	 * @param catalogerConfig
	 *            The configuration options the Cataloger will use
	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
	 *         Cataloger using an anonymous port. The <tt>cataloger.jar</tt> file
	 *         will be loaded from <tt>iGrid.home/lib</tt>
	 * 
	 * @throws IOException
	 *             If there are problems getting the anonymous port
	 * @throws RuntimeException
	 *             If the <tt>iGrid.home</tt> system property is not set
	 */
	public static ServiceDescriptor getCataloger(String policy,
			String hostAddress, int port, String... catalogerConfig)
			throws IOException {
		String fs = File.separator;
		String iGridHome = System.getProperty("iGrid.home");
		if (iGridHome == null)
			throw new RuntimeException("'iGrid.home' property not declared");
		
		// service provider classpath
		String catalogClasspath = ConfigUtil.concat(new Object[] {
				iGridHome,fs,"lib",fs,"sorcer",fs,"lib",fs,"cataloger.jar"
		});
		
		// service provider codebase
		String catalogCodebase = Booter.getCodebase(new String[] {
				"sorcer-prv-dl.jar", "cataloger-dl.jar", "jsk-dl.jar", "serviceui.jar", "exertlet-ui.jar" },
				hostAddress, Integer.toString(port));
		String implClass = "sorcer.core.provider.cataloger.ServiceCataloger";
		return (new SorcerServiceDescriptor(catalogCodebase, policy,
				catalogClasspath, implClass, catalogerConfig));

	}

	/**
	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
	 * {@link sorcer.core.provider.logger.ServiceLogger} using the Webster port created
	 * by this utility.
	 * 
	 * @param policy
	 *            The security policy file to use
	 * @param loggerConfig
	 *            The configuration options the ServiceLogger will use
	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
	 *         ServiceLogger using an anonymous port. The <tt>logger.jar</tt> file
	 *         will be loaded from <tt>iGrid.home/lib</tt>
	 * 
	 * @throws IOException
	 *             If there are problems getting the anonymous port
	 * @throws RuntimeException
	 *             If the <tt>iGrid.home</tt> system property is not set
	 */
	public static ServiceDescriptor getLogger(String policy,
			String... loggerConfig) throws IOException {
		return (getLogger(policy, Booter.getPort(), loggerConfig));
	}

	/**
	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
	 * {@link sorcer.core.provider.logger.ServiceLogger}.
	 * 
	 * @param policy
	 *            The security policy file to use
	 * @param port
	 *            The port to use when constructing the codebase
	 * @param loggerConfig
	 *            The configuration options the ServiceLogger will use
	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
	 *         ServiceLogger using an anonymous port. The <tt>logger.jar</tt> file
	 *         will be loaded from <tt>iGrid.home/lib</tt>
	 * 
	 * @throws IOException
	 *             If there are problems getting the anonymous port
	 * @throws RuntimeException
	 *             If the <tt>iGrid.home</tt> system property is not set
	 */
	public static ServiceDescriptor getLogger(String policy, int port,
			String... loggerConfig) throws IOException {
		return (getLogger(policy, Booter.getHostAddress(), port, loggerConfig));

	}

	/**
	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for
	 * {@link sorcer.core.provider.logger.ServiceLogger}.
	 * 
	 * @param policy
	 *            The security policy file to use
	 * @param hostAddress
	 *            The address to use when constructing the codebase
	 * @param port
	 *            The port to use when constructing the codebase
	 * @param loggerConfig
	 *            The configuration options the ServiceLogger will use
	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for the
	 *         ServiceLogger using an anonymous port. The <tt>logger.jar</tt> file
	 *         will be loaded from <tt>iGrid.home/lib</tt>
	 * 
	 * @throws IOException
	 *             If there are problems getting the anonymous port
	 * @throws RuntimeException
	 *             If the <tt>iGrid.home</tt> system property is not set
	 */
	public static ServiceDescriptor getLogger(String policy,
			String hostAddress, int port, String... loggerConfig)
			throws IOException {
		String fs = File.separator;
		String iGridHome = System.getProperty("iGrid.home");
		if (iGridHome == null)
			throw new RuntimeException("'iGrid.home' property not declared");
		
		// service provider classpath
		String loggerClasspath = ConfigUtil.concat(new Object[] {
				iGridHome,fs,"lib",fs,"sorcer",fs,"lib",fs,"logger.jar"
				//,ps,iGridHome,fs,"lib",fs,"sorcer",fs,"lib-dl",fs,"logger-ui.jar"
		});
		// service provider codebase
		String loggerCodebase = Booter.getCodebase(new String[] {
				"sorcer-prv-dl.jar", "logger-dl.jar", "jsk-dl.jar", "serviceui.jar" },
				hostAddress, Integer.toString(port));
		// Logger is a partner to ServiceTasker
		String implClass = "sorcer.core.provider.logger.ServiceLogger";
		return (new SorcerServiceDescriptor(loggerCodebase, policy,
				loggerClasspath, implClass, loggerConfig));

	}

	/**
	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for the
	 * Jini Lookup Service (Reggie), using the Webster port created by this
	 * utility.
	 * 
	 * @param policy
	 *            The security policy file to use
	 * @param lookupConfig
	 *            The configuration file Reggie will use
	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for
	 *         Reggie using an anonymous port. The <tt>reggie.jar</tt> file will
	 *         be loaded from <tt>JINI_HOME/lib</tt>
	 * 
	 * @throws IOException
	 *             If there are problems getting the anonymous port
	 * @throws RuntimeException
	 *             If the <tt>JINI_HOME</tt> system property is not set
	 */
	public static ServiceDescriptor getLookup(String policy,
			String... lookupConfig) throws IOException {
		return (getLookup(policy, Booter.getPort(), lookupConfig));
	}

	/**
	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for the
	 * Jini Lookup Service, Reggie.
	 * 
	 * @param policy
	 *            The security policy file to use
	 * @param lookupConfig
	 *            The configuration options Reggie will use
	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for
	 * @param port
	 *            The port to use when constructing the codebase Reggie using an
	 *            anonymous port. The <tt>reggie.jar</tt> file will be loaded
	 *            from <tt>JINI_HOME/lib</tt>
	 * 
	 * @throws IOException
	 *             If there are problems getting the anonymous port
	 * @throws RuntimeException
	 *             If the <tt>JINI_HOME</tt> system property is not set
	 */
	public static ServiceDescriptor getLookup(String policy, int port,
			String... lookupConfig) throws IOException {
		return (getLookup(policy, Booter.getHostAddress(), port, lookupConfig));
	}

	/**
	 * Get the {@link com.sun.jini.start.ServiceDescriptor} instance for the
	 * Jini Lookup Service (Reggie), using the Webster port created by this
	 * utility.
	 * 
	 * @param policy
	 *            The security policy file to use
	 * @return The {@link com.sun.jini.start.ServiceDescriptor} instance for
	 * @param hostAddress
	 *            The address to use when constructing the codebase
	 * @param port
	 *            The port to use when constructing the codebase Reggie using an
	 *            anonymous port. The <tt>reggie.jar</tt> file will be loaded
	 *            from <tt>JINI_HOME/lib</tt>
	 * @param lookupConfig
	 *            The configuration options Reggie will use
	 * 
	 * @throws IOException
	 *             If there are problems getting the anonymous port
	 * @throws RuntimeException
	 *             If the <tt>JINI_HOME</tt> system property is not set
	 */
	public static ServiceDescriptor getLookup(String policy,
			String hostAddress, int port, String... lookupConfig)
			throws IOException {
		String fs = File.separator;
		String iGridHome = System.getProperty("iGrid.home");
		if (iGridHome == null)
			throw new RuntimeException("'iGrid.home' system property not declared");
		String jiniHome = iGridHome+fs+"lib"+fs+"river";
		String reggieClasspath = jiniHome+fs+"lib"+fs+"reggie.jar";
		String reggieCodebase = Booter.getCodebase(new String[] {
				"reggie-dl.jar", "jsk-dl.jar", "sorcer-prv-dl.jar" }, 
				hostAddress, Integer.toString(port));
		String implClass = "com.sun.jini.reggie.TransientRegistrarImpl";
		return (new SorcerServiceDescriptor(reggieCodebase, policy,
				reggieClasspath, implClass, lookupConfig));
	}
	
	protected static String[] getArray(String s, String[] array) {
		String[] sArray;
		if (array != null && array.length > 0) {
			sArray = new String[array.length + 1];
			sArray[0] = s;
			System.arraycopy(array, 0, sArray, 1, sArray.length - 1);
		} else {
			sArray = new String[] { s };
		}
		return (sArray);
	}

	/**
	 * Concatenate the strings resulting from calling
	 * {@link java.lang.String#valueOf(Object)} on each element of an array of
	 * objects with a follow up separator. Passing a zero length array will
	 * result in the empty string being returned.
	 * 
	 * @param objects
	 *            the array of objects to be processed.
	 * @param objects
	 *            a character separator.
	 * @return the concatenation of the return values from calling
	 *         <code>String.valueOf</code> on each element of
	 *         <code>objects</code>.
	 * @throws NullPointerException
	 *             if <code>objects</code> is <code>null</code>.
	 */
	public static String concat(Object[] objects, char separator) {
		if (objects.length == 0)
			return "";
		int tally = objects.length;
		final StringBuffer buf = new StringBuffer(String.valueOf(objects[0]));
		if (tally > 1)
			buf.append(separator);
		for (int i = 1; i < tally - 1; i++)
			buf.append(objects[i]).append(separator);

		buf.append(objects[tally - 1]);
		return buf.toString();
	}

	/**
	 * Returns the home directory of the iGrid environment.
	 * 
	 * @return a path of the home directory
	 */
	public static String getHomeDir() {
		String hd = System.getenv("IGRID_HOME");

		if (hd != null && hd.length() > 0) {
			return hd;
		} else {
			hd = System.getProperty("iGrid.home");
			if (hd != null && hd.length() > 0) {
				return hd;
			}
		}
		throw new IllegalArgumentException(hd
				+ " is not a vald iGrid home directory");
	}

}