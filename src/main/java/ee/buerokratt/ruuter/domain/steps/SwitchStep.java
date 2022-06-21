package ee.buerokratt.ruuter.domain.steps;

import com.fasterxml.jackson.annotation.JsonAlias;
import ee.buerokratt.ruuter.domain.Condition;
import ee.buerokratt.ruuter.domain.ConfigurationInstance;
import ee.buerokratt.ruuter.helper.ScriptingHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Optional;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class SwitchStep extends ConfigurationStep {
    @JsonAlias({"switch"})
    private List<Condition> conditions;

    @Override
    public void execute(ConfigurationInstance configurationInstance) {
        super.execute(configurationInstance);
        ScriptingHelper scriptingHelper = configurationInstance.getScriptingHelper();
        Optional<Condition> correctStatement = conditions.stream()
            .filter(condition -> scriptingHelper.containsScript(condition.getConditionStatement())
                && Boolean.TRUE.equals(scriptingHelper.evaluateScripts(condition.getConditionStatement(), configurationInstance.getContext())))
            .findFirst();
        correctStatement.ifPresent(condition -> this.setNextStepName(condition.getNextStepName()));
    }
}
