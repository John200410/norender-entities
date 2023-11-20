package me.john200410;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.rusherhack.client.api.RusherHackAPI;
import org.rusherhack.client.api.events.render.EventRenderEntity;
import org.rusherhack.client.api.feature.module.IModule;
import org.rusherhack.client.api.feature.module.Module;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.plugin.Plugin;
import org.rusherhack.core.event.subscribe.Subscribe;
import org.rusherhack.core.setting.BooleanSetting;
import org.rusherhack.core.setting.Setting;

import java.util.HashMap;

/**
 * RusherHack plugin that adds the ability to disable rendering of specific entity types
 *
 * @author John200410
 */
public class CustomNoRender extends Plugin {
	
	@Override
	public void onLoad() {
		
		//logger
		this.getLogger().info(this.getName() + " loaded!");
		
		//custom setting to add to NoRender
		final BooleanSetting custom = new BooleanSetting("CustomEntities", false);
		
		//add dummy norenderlistener module
		//only temporary until i add the ability to add listeners to the event bus from the api
		final NoRenderListener customNoRenderModule = new NoRenderListener(custom);
		RusherHackAPI.getModuleManager().registerFeature(customNoRenderModule);
		
		//get NoRender module instance
		final IModule noRenderModule = RusherHackAPI.getModuleManager().getFeature("NoRender").get();
		
		//copy settings from dummy module into the real norender module
		for(Setting<?> entityTypeSetting : customNoRenderModule.getSettings()) {
			custom.addSubSettings(entityTypeSetting);
		}
		noRenderModule.registerSettings(custom);
	}
	
	@Override
	public void onUnload() {
		this.getLogger().info(this.getName() + " unloaded!");
	}
	
	@Override
	public String getName() {
		return "CustomNoRender";
	}
	
	@Override
	public String getVersion() {
		return "v1.0";
	}
	
	@Override
	public String getDescription() {
		return "Adds the ability to disable rendering of specific entity types";
	}
	
	@Override
	public String[] getAuthors() {
		return new String[]{"John200410"};
	}
	
	//dummy module temporary until i add ability to add listeners to the event bus from the api
	static class NoRenderListener extends Module {
		
		private final BooleanSetting setting;
		final HashMap<EntityType<?>, BooleanSetting> settingMap = new HashMap<>();
		
		public NoRenderListener(BooleanSetting setting) {
			super("CustomNoRender", ModuleCategory.RENDER);
			this.setting = setting;
			
			//shouldnt be visible in clickgui because we are just creating this module to use its event listener
			//lol i just realized i never implemented hidden modules in rusherhack so this doesnt do anything atm
			this.setHidden(true);
			
			//register settings
			for(EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
				
				//add boolean setting for each entity type
				final BooleanSetting entityTypeSetting = new BooleanSetting(entityType.getDescription().getString(), false);
				this.registerSettings(entityTypeSetting);
				this.settingMap.put(entityType, entityTypeSetting);
			}
		}
		
		@Subscribe
		private void onRenderEntity(EventRenderEntity event) {
			final Entity entity = event.getEntity();
			final EntityType<?> entityType = entity.getType();
			
			if(this.settingMap.containsKey(entityType) && this.settingMap.get(entityType).getValue()) {
				event.setCancelled(true);
			}
		}
		
		@Override
		public boolean isListening() {
			return this.setting.getValue();
		}
	}
	
}
