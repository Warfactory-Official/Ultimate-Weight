package com.warfactory.ultimateweight.v1122.groovy;

import com.cleanroommc.groovyscript.compat.mods.GroovyPropertyContainer;

/**
 * GroovyScript property container for Ultimate Weight. The public {@link #config} field is picked up
 * automatically by GroovyScript and exposed to scripts as {@code mods.wfweight.config}.
 */
public class UltimateWeightGroovyContainer extends GroovyPropertyContainer {

    public final WeightGroovyConfig config = new WeightGroovyConfig();
}
