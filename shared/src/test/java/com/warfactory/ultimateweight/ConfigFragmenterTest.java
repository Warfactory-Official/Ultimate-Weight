package com.warfactory.ultimateweight;

import com.warfactory.ultimateweight.network.ConfigFragment;
import com.warfactory.ultimateweight.network.ConfigFragmenter;
import com.warfactory.ultimateweight.network.ConfigReassembler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class ConfigFragmenterTest {
    @Test
    void fragmentsRoundTripAcrossOutOfOrderDelivery() {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < 400; index++) {
            builder.append("entry").append(index).append(": ").append(index).append('\n');
        }
        String yaml = builder.toString();

        ConfigFragmenter fragmenter = new ConfigFragmenter(128);
        List<ConfigFragment> fragments = fragmenter.fragment(yaml);
        Assertions.assertTrue(fragments.size() > 1);

        ConfigReassembler reassembler = new ConfigReassembler();
        String assembled = null;
        for (int index = fragments.size() - 1; index >= 0; index--) {
            assembled = reassembler.accept(fragments.get(index));
        }

        Assertions.assertEquals(yaml, assembled);
    }
}
