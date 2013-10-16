//
// Copyright (c) 2011 Hyperfine Software Corp.
// All rights reserved
//
package com.hyperfine.slideshare;

import java.util.GregorianCalendar;

import android.os.Build;

public class Config
{
	// Build string to appear in Information page
	public static final String buildString = "g000000000000";

    // Is this an Amazon release?
    public static final boolean isAmazon = false;
    
    // Recent search authority string
    public static final String RECENT_AUTHORITY = "authority.slideshare.hyperfine.com";

    // Default setting for notifications
    public static final boolean NOTIFICATIONS_ON_BY_DEFAULT = true;

    // Default base url
    public static final String baseSlideShareUrl = "http://hyperfine.com/slideshare/";

	// Error logs - ship with this set to false
	public static final boolean E = true;
	
	// Debug logs - ship with this set to false
	public static final boolean D = true;
	
	// Verbose logs - ship with this set to false
	public static final boolean V = true;
}
