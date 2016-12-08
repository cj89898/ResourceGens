package net.cjservers.resourcegens.objects;

import org.bukkit.Material;


public class ResourceGen {
	
	private String owner;
	private String world;
	private String type;
	private int x;
	private int y;
	private int z;
	private int level;
	private Material resource;

	public ResourceGen(String owner, String type, String world, int x, int y, int z, int level, Material resource) {
		this.owner = owner;
		this.type = type;
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.resource = resource;
		this.level = level;
	}

	public String getOwner(){
		return owner;
	}
	
	public String getType(){
		return type;
	}
	
	public String getWorld(){
		return world;
	}

	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getZ() {
		return z;
	}

	public int getLevel() {
		return level;
	}
	
	public Material getResource() {
		return resource;
	}
	
}
