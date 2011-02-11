package imagej.plugin.api;

import imagej.plugin.PluginHandler;


public class DefaultPluginHandlerFactory implements PluginHandlerFactory {

	@Override
	public PluginHandler createPluginHandler(PluginEntry entry)
		throws PluginException
	{
		return new PluginHandler(entry);
	}

}
