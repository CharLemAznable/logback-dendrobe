package com.github.charlemaznable.logback.miner.level;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.status.WarnStatus;
import lombok.NoArgsConstructor;
import lombok.val;

import static java.util.Objects.isNull;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class EffectorContextElf {

    public static final String EFFECTOR_CONTEXT = "EFFECTOR_CONTEXT";

    public static EffectorContext getEffectorContext(Context context) {
        val effectorContext = context.getObject(EFFECTOR_CONTEXT);
        if (isNull(effectorContext)) {
            context.getStatusManager().add(new WarnStatus(
                    "Get EffectorContext never been initialized", context));
        }
        return (EffectorContext) effectorContext;
    }
}
