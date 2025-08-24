<template>
  <CalendarRoot
    v-model="modelValue" 
    v-bind="forwarded"
    :class="cn('p-3', props.class)"
    calendar-label="Event Date"
  >
    <CalendarHeader>
      <CalendarPrev />
      <CalendarHeading />
      <CalendarNext />
    </CalendarHeader>

    <CalendarGrid>
      <CalendarCellTrigger />
    </CalendarGrid>
  </CalendarRoot>
</template>

<script setup lang="ts">
import type { CalendarRootEmits, CalendarRootProps } from 'reka-ui'
import {
  CalendarRoot,
  CalendarHeader,
  CalendarPrev,
  CalendarHeading,
  CalendarNext,
  CalendarGrid,
  CalendarCellTrigger,
  useForwardPropsEmits
} from 'reka-ui'
import { cn } from '@/lib/utils'

const props = defineProps<CalendarRootProps & { class?: string }>()
const emits = defineEmits<CalendarRootEmits>()
const forwarded = useForwardPropsEmits(props, emits)

const modelValue = defineModel()
</script>
