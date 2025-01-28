import {MatchType, Scope, SimplePricingRule, Unit} from '../models/price-rule';

export function toPricingRuleSummary(rule: SimplePricingRule): string[] {
  let positionDescription: string | null = getPositionDescription(rule);
  let scopeDescription: string | null = getScopeDescription(rule);
  let limitDescription: string | null = getLimitDescription(rule);

  return [positionDescription, scopeDescription, limitDescription]
    .filter((description) => description !== null)
}

function getPositionDescription(rule: SimplePricingRule): string | null {
  if (rule.positionMatchType == MatchType.EQUALS) {
    return `Match ${capitalizeFirstLetter( rule.positionReference)}`
  } else if (rule.positionMatchType == MatchType.HIGHER || rule.positionMatchType == MatchType.LOWER) {
    return `${rule.positionValue}${toUnitString(rule.positionUnit)} ${rule.positionMatchType.toLowerCase()} than ${capitalizeFirstLetter(rule.positionReference)}`
  } else {
    return null;
  }
}

function getScopeDescription(rule: SimplePricingRule) {
  switch (rule.scope) {
    case Scope.CATEGORY: return 'Scope: Category';
    case Scope.PRODUCT: return 'Scope: Product';
    default: return null;
  }
}

function getLimitDescription(rule: SimplePricingRule) {
  if(rule.hasMaxLimit && rule.hasMinLimit) return 'Max & Min Limit'
  if(!rule.hasMaxLimit && rule.hasMinLimit) return 'Min Limit'
  if(rule.hasMaxLimit && !rule.hasMinLimit) return 'Max Limit'
  return 'No Limit';
}

function toUnitString(unit: Unit) {
  if(unit == Unit.EUR) return '€';
  else if (unit == Unit.PERCENTAGE) return '%';
  else {
    console.warn(`Undefined unit: ${unit}`)
    return null;
  }
}

function capitalizeFirstLetter(str: string) {
  if (str.length === 0) return str;  // Return empty string if input is empty
  return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
}
