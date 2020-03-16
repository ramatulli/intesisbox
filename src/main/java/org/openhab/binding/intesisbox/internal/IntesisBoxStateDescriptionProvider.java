package org.openhab.binding.intesisbox.internal;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.type.DynamicStateDescriptionProvider;
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateDescriptionFragmentBuilder;
import org.eclipse.smarthome.core.types.StateOption;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * Dynamic provider of state options while leaving other state description fields as original.
 *
 * @author Gregory Moyer - Initial contribution
 * @author Rocky Amatulli - Adapted for IntesisBox binding
 */

@Component(service = { DynamicStateDescriptionProvider.class, IntesisBoxStateDescriptionProvider.class })
@NonNullByDefault
public class IntesisBoxStateDescriptionProvider implements DynamicStateDescriptionProvider {
    private final Map<ChannelUID, @Nullable List<StateOption>> channelStateOptionsMap = new ConcurrentHashMap<>();
    private final Map<ChannelUID, @Nullable Map<String, BigDecimal>> channelStateLimitsMap = new ConcurrentHashMap<>();

    public void setStateOptions(ChannelUID channelUID, List<StateOption> options) {
        channelStateOptionsMap.put(channelUID, options);
    }

    public void setStateLimits(ChannelUID channelUID, Map<String, BigDecimal> limits) {
        channelStateLimitsMap.put(channelUID, limits);
    }

    @Override
    public @Nullable StateDescription getStateDescription(Channel channel, @Nullable StateDescription original,
            @Nullable Locale locale) {

        List<StateOption> options = channelStateOptionsMap.get(channel.getUID());
        Map<String, BigDecimal> limits = channelStateLimitsMap.get(channel.getUID());

        StateDescriptionFragmentBuilder builder = (original == null) ? StateDescriptionFragmentBuilder.create()
                : StateDescriptionFragmentBuilder.create(original);

        if (limits != null) {
            builder.withMinimum(limits.get("min"));
            builder.withMaximum(limits.get("max"));
        }
        if (options != null) {
            builder.withOptions(options);
        }
        return builder.build().toStateDescription();

    }

    @Deactivate
    public void deactivate() {
        channelStateOptionsMap.clear();
        channelStateLimitsMap.clear();
    }

}
