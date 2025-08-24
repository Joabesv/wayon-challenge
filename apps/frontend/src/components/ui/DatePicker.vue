<template>
  <div :class="cn('grid gap-2', props.class)">
    <Input
      type="date"
      v-model="modelValue"
      :min="minDate"
      :class="cn('w-full', inputClass)"
      v-bind="$attrs"
    />
  </div>
</template>

<script setup lang="ts">
import { cn } from '@/lib/utils'
import Input from '@/components/ui/Input.vue'

export interface DatePickerProps {
  class?: string
  inputClass?: string
  minDate?: string
}

const props = withDefaults(defineProps<DatePickerProps>(), {})
const modelValue = defineModel<string>()
</script>
