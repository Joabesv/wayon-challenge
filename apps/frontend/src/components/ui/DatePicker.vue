<template>
  <Popover>
    <PopoverTrigger as-child>
      <Button
        variant="outline"
        :class="cn(
          'w-full justify-start text-left font-normal',
          !calendarValue && 'text-muted-foreground'
        )"
      >
        <CalendarIcon class="mr-2 h-4 w-4" />
        {{ calendarValue ? df.format(calendarValue.toDate(getLocalTimeZone())) : placeholder }}
      </Button>
    </PopoverTrigger>
    <PopoverContent class="w-auto p-0" align="start">
      <Calendar
        v-model="calendarValue"
        :is-date-disabled="handleIsDateDisabled"
        initial-focus
      />
    </PopoverContent>
  </Popover>
</template>

<script setup lang="ts">
import type { DateValue } from '@internationalized/date'
import {
  DateFormatter,
  getLocalTimeZone,
  parseDate
} from '@internationalized/date'
import { CalendarIcon } from 'lucide-vue-next'
import { ref, watch } from 'vue'
import { Button } from '@/components/ui/button'
import { Calendar } from '@/components/ui/calendar'
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover'
import { cn } from '@/lib/utils'

interface Props {
  placeholder?: string
  disabledDate?: (date: Date) => boolean
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: 'Selecione uma data'
})

// Use defineModel for v-model best practices (Vue 3.4+)
const model = defineModel<Date | undefined>()

const df = new DateFormatter('pt-BR', {
  dateStyle: 'long'
})

const calendarValue = ref<DateValue>()

const handleIsDateDisabled = (date: DateValue) => {
  if (!props.disabledDate) return false
  
  try {
    // Convert DateValue to JS Date for the disabled check
    const jsDate = date.toDate(getLocalTimeZone())
    return props.disabledDate(jsDate)
  } catch (error) {
    console.error('Error in date disabled check:', error)
    return false
  }
}

// Watch for changes to calendarValue and sync with model
watch(calendarValue, (newValue) => {
  if (newValue) {
    try {
      // Convert DateValue to JavaScript Date
      const jsDate = newValue.toDate(getLocalTimeZone())
      // Only update if the date actually changed to prevent infinite loops
      if (!model.value || model.value.getTime() !== jsDate.getTime()) {
        model.value = jsDate
      }
    } catch (error) {
      console.error('Error converting date:', error)
      model.value = undefined
    }
  } else {
    model.value = undefined
  }
})

watch(model, (newValue) => {
  try {
    if (newValue && newValue instanceof Date && !isNaN(newValue.getTime())) {
      // Convert JavaScript Date to DateValue
      const year = newValue.getFullYear()
      const month = newValue.getMonth() + 1
      const day = newValue.getDate()
      const dateString = `${year}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}`
      
      // Only update if the calendar value is different to prevent infinite loops
      const currentDateString = calendarValue.value ? calendarValue.value.toString() : null
      if (currentDateString !== dateString) {
        console.log('Updating calendar value to:', dateString, newValue)
        calendarValue.value = parseDate(dateString)
      }
    } else {
      if (calendarValue.value !== undefined) {
        calendarValue.value = undefined
      }
    }
  } catch (error) {
    console.error('Error updating calendar value:', error)
    calendarValue.value = undefined
  }
}, { immediate: true })
</script>
