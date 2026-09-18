import { test, expect } from '@playwright/test'

test.describe('Discover View', () => {
  test('should load discovery view and render hero text', async ({ page }) => {
    await page.goto('/')
    await expect(page.locator('h1')).toContainText('Discover Palettes')
    await expect(page.locator('.filter-bar')).toBeVisible()
    await expect(page.locator('.sort-tabs')).toBeVisible()
  })

  test('navigation links are responsive and accessible', async ({ page }) => {
    await page.goto('/')
    await expect(page.locator('.brand-name')).toContainText('Palette')
  })
})
